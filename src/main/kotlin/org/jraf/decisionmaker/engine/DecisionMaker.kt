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

package org.jraf.decisionmaker.engine

import org.jraf.decisionmaker.util.rounded

class DecisionMaker {
    private val allCriteria = linkedSetOf<Criteria>()
    private val data = mutableMapOf<Choice, Map<Criteria, Score>>()

    fun setCriteriaValuesForChoice(choice: Choice, vararg criteriaValue: Pair<Criteria, Score>) {
        var criteriaToValue = data.getOrPut(choice) { mapOf() }
        criteriaToValue += criteriaValue
        data[choice] = criteriaToValue

        allCriteria += criteriaValue.map { it.first }
    }

    fun computeResults(): List<Result> {
        val result = mutableListOf<Result>()
        for ((choice, criteriaScores) in data) {
            var totalScore = 0.0

            val criteriaScoresWithMissingScores = getCriteriaScoresWithMissingScores(criteriaScores, allCriteria)

            for ((criteria, score) in criteriaScoresWithMissingScores) {
                val scoreForCriteria = score.value * criteria.importance
                totalScore += scoreForCriteria
            }

            result += Result(choice = choice, score = totalScore)
        }
        return result.sortedByDescending { it.score }
    }


    fun getDataTable(results: List<Result>): List<List<String>> {
        val table = mutableListOf<List<String>>()
        var row = listOf<String>()
        row += ""
        for (result in results) {
            row += result.choice.name
        }
        table += row

        row = listOf()
        row += "Rank"
        for (i in results.indices) {
            row += "#${i + 1}"
        }
        table += row

        for (criteria in allCriteria) {
            row = listOf()
            row += "${criteria.name} (${criteria.importance})"
            for (result in results) {
                row += "${data[result.choice]?.get(criteria) ?: "-"}"
            }
            table += row
        }

        row = listOf()
        row += "Score"
        for (result in results) {
            row += result.score.rounded().toString()
        }
        table += row

        return table
    }

    private fun getCriteriaScoresWithMissingScores(
        criteriaScores: Map<Criteria, Score>,
        allCriteria: Set<Criteria>,
    ): Map<Criteria, Score> = allCriteria.associateWith { criteria -> criteriaScores[criteria] ?: Score.AVERAGE }
}

data class Criteria(
    val name: String,
    val importance: Double,
)

data class Choice(val name: String)

data class Result(
    val choice: Choice,
    val score: Double,
)

value class Score(val value: Double) {
    init {
        require(value >= 0.0) { "A Score must be positive and should be and <=10" }
    }

    override fun toString() = value.toString()

    companion object {
        val AVERAGE = Score(5.0)
    }
}
