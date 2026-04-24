package com.example.universalprinter.printer.adapters

import android.content.Context
import android.hardware.usb.UsbConstants
import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbDeviceConnection
import android.hardware.usb.UsbEndpoint
import android.hardware.usb.UsbInterface
import android.hardware.usb.UsbManager
import com.example.universalprinter.model.PrintJob
import com.example.universalprinter.printer.EscPosReceiptEncoder
import com.example.universalprinter.printer.Printer
import com.example.universalprinter.printer.PrinterException
import com.example.universalprinter.printer.UsbPermissionHelper
import kotlin.math.min
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class UsbEscPosPrinter(
    context: Context,
    private val vendorId: Int? = null,
    private val productId: Int? = null,
    private val charsetName: String
) : Printer {

    private val usbManager =
        context.applicationContext.getSystemService(Context.USB_SERVICE) as UsbManager
    private val appContext = context.applicationContext

    private var connection: UsbDeviceConnection? = null
    private var usbInterface: UsbInterface? = null
    private var outEndpoint: UsbEndpoint? = null

    override suspend fun connect() = withContext(Dispatchers.IO) {
        val selected = selectDevice()
            ?: throw PrinterException("找不到可用的 USB 印表機")

        val granted = UsbPermissionHelper.ensurePermission(
            appContext,
            usbManager,
            selected.device
        )
        if (!granted) {
            throw PrinterException("USB 權限被拒絕")
        }

        val conn = usbManager.openDevice(selected.device)
            ?: throw PrinterException("USB 開啟失敗")

        if (!conn.claimInterface(selected.usbInterface, true)) {
            conn.close()
            throw PrinterException("USB claimInterface 失敗")
        }

        connection = conn
        usbInterface = selected.usbInterface
        outEndpoint = selected.outEndpoint
    }

    override suspend fun print(job: PrintJob) = withContext(Dispatchers.IO) {
        val conn = connection ?: throw PrinterException("USB 尚未連線")
        val endpoint = outEndpoint ?: throw PrinterException("找不到 USB OUT Endpoint")
        val data = EscPosReceiptEncoder.build(job, charsetName)

        var offset = 0
        while (offset < data.size) {
            val len = min(4096, data.size - offset)
            val chunk = data.copyOfRange(offset, offset + len)
            val written = conn.bulkTransfer(endpoint, chunk, chunk.size, 5000)
            if (written <= 0) {
                throw PrinterException("USB 列印失敗，bulkTransfer=$written")
            }
            offset += written
        }
    }

    override suspend fun disconnect() = withContext(Dispatchers.IO) {
        runCatching {
            if (connection != null && usbInterface != null) {
                connection?.releaseInterface(usbInterface)
            }
        }
        runCatching { connection?.close() }
        connection = null
        usbInterface = null
        outEndpoint = null
    }

    private fun selectDevice(): UsbSelection? {
        val devices = usbManager.deviceList.values.toList()
            .filter { device ->
                (vendorId == null || device.vendorId == vendorId) &&
                    (productId == null || device.productId == productId)
            }

        for (device in devices) {
            for (i in 0 until device.interfaceCount) {
                val intf = device.getInterface(i)
                for (j in 0 until intf.endpointCount) {
                    val ep = intf.getEndpoint(j)
                    if (ep.type == UsbConstants.USB_ENDPOINT_XFER_BULK &&
                        ep.direction == UsbConstants.USB_DIR_OUT
                    ) {
                        return UsbSelection(device, intf, ep)
                    }
                }
            }
        }
        return null
    }

    private data class UsbSelection(
        val device: UsbDevice,
        val usbInterface: UsbInterface,
        val outEndpoint: UsbEndpoint
    )
}
