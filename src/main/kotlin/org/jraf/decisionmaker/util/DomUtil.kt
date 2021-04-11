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

import kotlinx.browser.document
import org.w3c.dom.Element
import kotlin.properties.ReadOnlyProperty

inline fun <reified T : Element> domId(): ReadOnlyProperty<Any?, T> = ReadOnlyProperty { _, property -> elementById(property.name) }

inline fun <reified T : Element> elementById(id: String): T = document.getElementById(id) as T

inline fun <reified T : Element> T.addEventListener(type: String, crossinline listener: (target: T) -> Unit) =
    addEventListener(type, { listener(it.target as T) })