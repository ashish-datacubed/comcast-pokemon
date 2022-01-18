package com.ashish.comcastpokemon.utils

object StringUtil {

    fun commaSeparatedStrings(arr: List<String>): String {
        val sb = StringBuilder()
        arr.forEach {
            sb.append(it).append(", ")
        }
        sb.deleteRange(sb.length - 2, sb.length)
        return sb.toString()
    }
}