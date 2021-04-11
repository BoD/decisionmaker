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

package org.jraf.decisionmaker.util

typealias Content = StringBuilder.() -> Unit

fun html(content: StringBuilder.() -> String) = StringBuilder().content()

fun StringBuilder.tag(
    tag: String,
    attributes: Map<String, Any>? = null,
    content: Content? = null,
): String {
    append("<$tag")
    if (attributes != null) {
        for (attribute in attributes) {
            append(" ")
            append(attribute.key)
            append("='")
            append(attribute.value)
            append("'")
        }
    }
    if (content != null) {
        append(">")
        content(this)
        append("</$tag>")
    } else {
        append(" />")
    }
    return this.toString()
}

fun StringBuilder.table(content: Content) = tag("table", content = content)
fun StringBuilder.tr(content: Content) = tag("tr", content = content)
fun StringBuilder.td(colspan: Int = -1, content: Content) = tag("td", if (colspan > 0) mapOf("colspan" to colspan) else null, content)

fun StringBuilder.input(
    id: String,
    type: String = "text",
    placeHolder: String? = null,
    value: String? = null,
    min: Int? = null,
    size: Int? = null,
) =
    tag("input", mutableMapOf("id" to id, "type" to type).apply {
        if (placeHolder != null) put("placeholder", placeHolder)
        if (value != null) put("value", value)
        if (min != null) put("min", min.toString())
        if (size != null) put("size", size.toString())
    })

fun StringBuilder.button(id: String, content: Content) = tag("button", mapOf("type" to "button", "id" to id), content = content)