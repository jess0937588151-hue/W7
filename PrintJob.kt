package com.example.universalprinter.model

data class PrintJob(
    val lines: List<PrintLine>,
    val qrContent: String? = null,
    val feedLines: Int = 4,
    val cut: Boolean = true,
    val openCashDrawer: Boolean = false
)
