package com.example.universalprinter.printer

import android.content.Context
import com.example.universalprinter.model.PrinterConnectionConfig
import com.example.universalprinter.model.PrinterType
import com.example.universalprinter.model.PrintJob
import com.example.universalprinter.printer.adapters.BluetoothEscPosPrinter
import com.example.universalprinter.printer.adapters.SunmiPrinter
import com.example.universalprinter.printer.adapters.TcpEscPosPrinter
import com.example.universalprinter.printer.adapters.UsbEscPosPrinter

class PrinterManager(private val context: Context) {

    suspend fun print(config: PrinterConnectionConfig, job: PrintJob) {
        val printer = when (config.type) {
            PrinterType.SUNMI -> SunmiPrinter(context, config.charsetName)
            PrinterType.TCP_ESC_POS -> TcpEscPosPrinter(
                host = config.ip ?: throw PrinterException("請輸入 IP"),
                port = config.port,
                charsetName = config.charsetName
            )
            PrinterType.BLUETOOTH_ESC_POS -> BluetoothEscPosPrinter(
                macAddress = config.bluetoothMac ?: throw PrinterException("請輸入藍牙 MAC"),
                charsetName = config.charsetName
            )
            PrinterType.USB_ESC_POS -> UsbEscPosPrinter(
                context = context,
                charsetName = config.charsetName
            )
        }

        try {
            printer.connect()
            printer.print(job)
        } finally {
            runCatching { printer.disconnect() }
        }
    }
}
