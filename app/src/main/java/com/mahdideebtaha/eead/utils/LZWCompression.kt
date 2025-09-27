package com.mahdideebtaha.eead.utils

object LZWCompression {

    fun compress(input: ByteArray): List<Int> {
        val inputStr = input.toString(Charsets.ISO_8859_1)

        val dictionary = mutableMapOf<String, Int>()
        for (i in 0..255) dictionary[i.toChar().toString()] = i

        var current = ""
        val result = mutableListOf<Int>()
        var dictSize = 256

        for (char in inputStr) {
            val combined = current + char
            if (dictionary.containsKey(combined)) {
                current = combined
            } else {
                result.add(dictionary[current]!!)
                dictionary[combined] = dictSize++
                current = char.toString()
            }
        }

        if (current.isNotEmpty()) {
            result.add(dictionary[current]!!)
        }

        return result
    }
}