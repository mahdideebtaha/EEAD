package com.mahdideebtaha.eead.model


data class HuffmanNode(
    val byte: Byte?,
    val freq: Int,
    val left: HuffmanNode? = null,
    val right: HuffmanNode? = null
)

