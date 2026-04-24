package com.example.universalprinter.model

enum class PrinterType(val label: String) {
    SUNMI("SUNMI 內建印表機"),
    TCP_ESC_POS("網路印表機 (TCP/ESC-POS)"),
    BLUETOOTH_ESC_POS("藍牙印表機 (BT/ESC-POS)"),
    USB_ESC_POS("USB 印表機 (USB/ESC-POS)")
}
