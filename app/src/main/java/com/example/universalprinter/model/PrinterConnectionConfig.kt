package com.example.universalprinter.model

data class PrinterConnectionConfig(
    val type: PrinterType,
    val ip: String? = null,
    val port: Int = 9100,
    val bluetoothMac: String? = null,
    val usbVendorId: Int? = null,
    val usbProductId: Int? = null,
    val charsetName: String = "Big5"
)

