package com.mahdideebtaha.eead.model

data class HuffmanNode(
    val char: Char? = null,
    val freq: Int,
    val left: HuffmanNode? = null,
    val right: HuffmanNode? = null
)
