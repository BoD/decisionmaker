/*
 * This source is part of the
 *      _____  ___   ____
 *  __ / / _ \/ _ | / __/___  _______ _
 * / // / , _/ __ |/ _/_/ _ \/ __/ _ `/
 * \___/_/|_/_/ |_/_/ (_)___/_/  \_, /
 *                              /___/
 * repository.
 *
 * Copyright (C) 2021-present Benoit 'BoD' Lubek (BoD@JRAF.org)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.jraf.decisionmaker.web

import kotlinx.browser.window
import org.jraf.decisionmaker.engine.Choice
import org.jraf.decisionmaker.engine.Criteria
import org.jraf.decisionmaker.engine.DecisionMaker
import org.jraf.decisionmaker.engine.Score
import org.jraf.decisionmaker.util.addEventListener
import org.jraf.decisionmaker.util.button
import org.jraf.decisionmaker.util.domId
import org.jraf.decisionmaker.util.elementById
import org.jraf.decisionmaker.util.html
import org.jraf.decisionmaker.util.input
import org.jraf.decisionmaker.util.table
import org.jraf.decisionmaker.util.td
import org.jraf.decisionmaker.util.toHtmlTable
import org.jraf.decisionmaker.util.tr
import org.jraf.decisionmaker.util.uriEncode
import org.w3c.dom.HTMLButtonElement
import org.w3c.dom.HTMLDivElement
import org.w3c.dom.HTMLInputElement
import org.w3c.dom.url.URLSearchParams

class Main {

    private val criteriaById = mutableMapOf<Int, Criteria>()
    private val choiceById = mutableMapOf<Int, Choice>()

    /**
     * Map of Choice id to (map of Criteria id to value).
     */
    private val criteriaValuesPerChoice = mutableMapOf<Int, MutableMap<Int, Double>>()

    private val ctnCriteria: HTMLDivElement by domId()
    private val ctnChoices: HTMLDivElement by domId()
    private val btnAddCriteria: HTMLButtonElement by domId()
    private val btnAddChoice: HTMLButtonElement by domId()
    private val ctnResults: HTMLDivElement by domId()

    private var idGenerator = 0
    private fun newId() = idGenerator++

    private fun buildCriteriaTable() = html {
        table {
            for ((id, criteria) in criteriaById) {
                tr {
                    td {
                        input(id = "inpCriteriaName$id", placeHolder = "Criteria", value = criteria.name)
                    }
                    td {
                        input(
                            id = "inpCriteriaImportance$id",
                            type = "number",
                            placeHolder = "Importance (can be <0)",
                            value = criteria.importance.toString(),
                        )
                    }
                    td {
                        button(id = "btnRemoveCriteria$id") { append("Remove") }
                    }
                }
            }
            tr {
                td(colspan = 3) {
                    button(id = "btnAddCriteria") { append("Add criteria") }
                }
            }
        }
    }

    private fun onAddCriteriaClick(@Suppress("UNUSED_PARAMETER") target: HTMLButtonElement) {
        criteriaById[newId()] = Criteria(name = "New criteria", importance = 5.0)
        updateCriteriaTable()
        updateChoicesTable()
        updateResults()
    }

    private fun updateCriteriaTable() {
        ctnCriteria.innerHTML = buildCriteriaTable()

        for (id in criteriaById.keys) {
            elementById<HTMLInputElement>("inpCriteriaName$id").addEventListener("input") { target ->
                criteriaById[id] = criteriaById[id]!!.copy(name = target.value)
                updateChoicesTable()
                updateResults()
            }

            elementById<HTMLInputElement>("inpCriteriaImportance$id").addEventListener("input") { target ->
                val importance = target.value.toDoubleOrNull() ?: return@addEventListener
                criteriaById[id] = criteriaById[id]!!.copy(importance = importance)
                updateChoicesTable()
                updateResults()
            }

            elementById<HTMLButtonElement>("btnRemoveCriteria$id").addEventListener("click") {
                removeCriteria(id)
            }
        }

        btnAddCriteria.addEventListener("click", ::onAddCriteriaClick)
    }

    private fun removeCriteria(id: Int) {
        criteriaById.remove(id)
        for (criteriaIdToValue in criteriaValuesPerChoice.values) {
            criteriaIdToValue.remove(id)
        }
        updateCriteriaTable()
        updateChoicesTable()
        updateResults()
    }

    private fun buildChoicesTable() = html {
        table {
            for ((choiceId, choice) in choiceById) {
                tr {
                    td(colspan = 2) {
                        input(id = "inpChoiceName$choiceId", placeHolder = "Choice", value = choice.name)
                        append("&nbsp;")
                        button(id = "btnRemoveChoice$choiceId") {
                            append("Remove")
                        }
                    }
                }

                for ((criteriaId, criteria) in criteriaById) {
                    val value = criteriaValuesPerChoice[choiceId]?.get(criteriaId) ?: ""
                    tr {
                        td {
                            append("${criteria.name}: ")
                        }
                        td {
                            input(
                                id = "inpCriteriaValue${choiceId}_$criteriaId",
                                type = "number",
                                placeHolder = "Score (0 to 10)",
                                value = value.toString(),
                                min = 0,
                            )
                        }
                    }
                }
                tr { td { append("&nbsp;") } }
            }
            tr {
                td(colspan = 2) {
                    button(id = "btnAddChoice") { append("Add choice") }
                }
            }
        }
    }

    private fun onAddChoiceClick(@Suppress("UNUSED_PARAMETER") target: HTMLButtonElement) {
        val newChoiceId = newId()
        choiceById[newChoiceId] = Choice("New choice")
        criteriaValuesPerChoice[newChoiceId] = mutableMapOf()
        updateChoicesTable()
        updateResults()
    }

    private fun removeChoice(id: Int) {
        choiceById.remove(id)
        criteriaValuesPerChoice.remove(id)
        updateChoicesTable()
        updateResults()
    }

    private fun updateChoicesTable() {
        ctnChoices.innerHTML = buildChoicesTable()
        for (choiceId in choiceById.keys) {
            elementById<HTMLInputElement>("inpChoiceName$choiceId").addEventListener("input") { target ->
                choiceById[choiceId] = choiceById[choiceId]!!.copy(name = target.value)

                updateResults()
            }

            elementById<HTMLButtonElement>("btnRemoveChoice$choiceId").addEventListener("click") {
                removeChoice(choiceId)
            }

            for (criteriaId in criteriaById.keys) {
                elementById<HTMLInputElement>("inpCriteriaValue${choiceId}_$criteriaId").addEventListener("input") { target ->
                    val choiceToValue = criteriaValuesPerChoice.getOrPut(choiceId) { mutableMapOf() }
                    val valueStr = target.value
                    if (valueStr.isBlank()) {
                        // Remove values left blank
                        choiceToValue.remove(criteriaId)
                    } else {
                        val value = valueStr.toDoubleOrNull()
                        if (value != null) choiceToValue[criteriaId] = value
                    }

                    updateResults()
                }
            }
        }
        btnAddChoice.addEventListener("click", ::onAddChoiceClick)
    }

    private fun updateResults() {
        val decisionMaker = DecisionMaker()
        for ((choiceId, criteriaIdValue) in criteriaValuesPerChoice) {
            val choice = choiceById[choiceId]!!
            val criteriaIdValueEntriesSortedById = criteriaIdValue.entries.sortedBy { it.key }
            for ((criteriaId, value) in criteriaIdValueEntriesSortedById) {
                val criteria = criteriaById[criteriaId]!!
                decisionMaker.setCriteriaValuesForChoice(choice, criteria to Score(value))
            }
        }
        val results = decisionMaker.computeResults()
        val resultsUrl = "${window.location.href.split('?')[0]}?data=${uriEncode(serializeModel())}"
        ctnResults.innerHTML = decisionMaker.getDataTable(results).toHtmlTable() +
            "<br><br>" +
            "<a href='$resultsUrl'>Link to this comparison</a>"
        if (window.location.href != resultsUrl) {
            window.history.pushState(null, "", resultsUrl)
        }
    }

    private fun serializeModel(): String {
        val res = StringBuilder(criteriaById.entries.joinToString("_") { "${it.key}~${it.value.name}~${it.value.importance}" })
            .append("__")
            .append(choiceById.entries.joinToString("_") { "${it.key}~${it.value.name}" })
            .append("__")
            .append(criteriaValuesPerChoice.entries.joinToString("_") { (choiceId, criteriaIdValue) ->
                choiceId.toString() + "-" + criteriaIdValue.entries.joinToString(" ") { "${it.key}~${it.value}" }
            })

        return res.toString()
    }

    private fun readDataFromUrl() {
        val serializedModel = URLSearchParams(window.location.search).get("data") ?: return
        val (serializedCriteriaById, serializedChoiceById, serializedCriteriaValuesPerChoice) = serializedModel.split("__")

        serializedCriteriaById.split('_').forEach {
            val (id, name, importance) = it.split('~')
            criteriaById[id.toInt()] = Criteria(name, importance.toDouble())
        }
        serializedChoiceById.split('_').forEach {
            if (!it.contains('~')) return@forEach
            val (id, name) = it.split('~')
            choiceById[id.toInt()] = Choice(name)
        }
        serializedCriteriaValuesPerChoice.split('_').forEach { choiceIdAllValues ->
            val choiceIdAllValuesSplit = choiceIdAllValues.split('-')
            if (choiceIdAllValuesSplit.size == 2) {
                val (choiceId, allValues) = choiceIdAllValuesSplit
                val valuesMap = mutableMapOf<Int, Double>()
                allValues.split(' ').forEach { criteriaIdValue ->
                    val criteriaIdValueSplit = criteriaIdValue.split('~')
                    if (criteriaIdValueSplit.size == 2) {
                        val (criteriaId, value) = criteriaIdValueSplit
                        valuesMap[criteriaId.toInt()] = value.toDouble()
                    }
                }
                criteriaValuesPerChoice[choiceId.toInt()] = valuesMap
            }
        }

        // Start id at the current highest id + 1
        idGenerator = ((criteriaById.keys + choiceById.keys).maxOrNull() ?: -1) + 1
    }

    fun start() {
        readDataFromUrl()
        updateCriteriaTable()
        updateChoicesTable()
        updateResults()
    }
}