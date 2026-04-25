package com.example.universalprinter.printer

import com.example.universalprinter.model.PrintJob

interface Printer {
    suspend fun connect()
    suspend fun print(job: PrintJob)
    suspend fun disconnect()
}
