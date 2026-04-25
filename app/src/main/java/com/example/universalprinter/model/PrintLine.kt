package com.example.universalprinter.model

data class PrintLine(
    val text: String,
    val align: Align = Align.LEFT,
    val bold: Boolean = false,
    val doubleSize: Boolean = false
)

