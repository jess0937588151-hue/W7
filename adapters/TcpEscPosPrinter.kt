package com.example.universalprinter.printer.adapters

import com.example.universalprinter.model.PrintJob
import com.example.universalprinter.printer.EscPosReceiptEncoder
import com.example.universalprinter.printer.Printer
import com.example.universalprinter.printer.PrinterException
import java.net.InetSocketAddress
import java.net.Socket
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class TcpEscPosPrinter(
    private val host: String,
    private val port: Int,
    private val charsetName: String
) : Printer {

    private var socket: Socket? = null

    override suspend fun connect() = withContext(Dispatchers.IO) {
        try {
            socket = Socket().apply {
                connect(InetSocketAddress(host, port), 5000)
                tcpNoDelay = true
                soTimeout = 5000
            }
        } catch (e: Exception) {
            throw PrinterException("TCP 連線失敗: $host:$port", e)
        }
    }

    override suspend fun print(job: PrintJob) = withContext(Dispatchers.IO) {
        val s = socket ?: throw PrinterException("TCP 尚未連線")
        val data = EscPosReceiptEncoder.build(job, charsetName)
        try {
            s.getOutputStream().use { out ->
                out.write(data)
                out.flush()
            }
        } catch (e: Exception) {
            throw PrinterException("TCP 列印失敗", e)
        }
    }

    override suspend fun disconnect() = withContext(Dispatchers.IO) {
        runCatching { socket?.close() }
        socket = null
    }
}
