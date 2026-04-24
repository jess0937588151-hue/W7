package com.example.universalprinter.printer.adapters

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothSocket
import com.example.universalprinter.model.PrintJob
import com.example.universalprinter.printer.EscPosReceiptEncoder
import com.example.universalprinter.printer.Printer
import com.example.universalprinter.printer.PrinterException
import java.util.UUID
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class BluetoothEscPosPrinter(
    private val macAddress: String,
    private val charsetName: String
) : Printer {

    private var socket: BluetoothSocket? = null

    @SuppressLint("MissingPermission")
    override suspend fun connect() = withContext(Dispatchers.IO) {
        val adapter = BluetoothAdapter.getDefaultAdapter()
            ?: throw PrinterException("裝置不支援藍牙")

        try {
            val device = adapter.getRemoteDevice(macAddress)
            adapter.cancelDiscovery()

            socket = device.createRfcommSocketToServiceRecord(SPP_UUID).apply {
                connect()
            }
        } catch (e: Exception) {
            throw PrinterException("藍牙連線失敗: $macAddress", e)
        }
    }

    override suspend fun print(job: PrintJob) = withContext(Dispatchers.IO) {
        val btSocket = socket ?: throw PrinterException("藍牙尚未連線")
        val data = EscPosReceiptEncoder.build(job, charsetName)

        try {
            btSocket.outputStream.write(data)
            btSocket.outputStream.flush()
        } catch (e: Exception) {
            throw PrinterException("藍牙列印失敗", e)
        }
    }

    override suspend fun disconnect() = withContext(Dispatchers.IO) {
        runCatching { socket?.close() }
        socket = null
    }

    companion object {
        private val SPP_UUID: UUID =
            UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")
    }
}
