package com.mahdideebtaha.eead.utils

import com.mahdideebtaha.eead.model.HuffmanNode
import java.util.PriorityQueue

object HuffmanCompression {
    fun compress(data: String): Pair<String, Map<Char, String>> {
        val freqMap = data.groupingBy { it }.eachCount()
        val queue = PriorityQueue(compareBy<HuffmanNode> { it.freq })
        freqMap.forEach { (char, freq) -> queue.add(HuffmanNode(char, freq)) }

        while (queue.size > 1) {
            val left = queue.poll()
            val right = queue.poll()
            queue.add(HuffmanNode(null, left.freq + right.freq, left, right))
        }

        val root = queue.poll()
        val codes = mutableMapOf<Char, String>()
        buildCodeTable(root, "", codes)

        val compressed = data.map { codes[it] }.joinToString("")
        return Pair(compressed, codes)
    }

    private fun buildCodeTable(node: HuffmanNode?, code: String, map: MutableMap<Char, String>) {
        if (node == null) return
        if (node.char != null) map[node.char] = code
        buildCodeTable(node.left, code + "0", map)
        buildCodeTable(node.right, code + "1", map)
    }
}