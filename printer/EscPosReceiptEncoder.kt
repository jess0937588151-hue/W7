package com.example.universalprinter.printer

import com.example.universalprinter.model.PrintJob
import java.io.ByteArrayOutputStream
import java.nio.charset.Charset

object EscPosReceiptEncoder {

    fun build(job: PrintJob, charsetName: String = "GBK"): ByteArray {
        val charset = try {
            Charset.forName(charsetName)
        } catch (_: Exception) {
            Charsets.UTF_8
        }

        val out = ByteArrayOutputStream()

        fun write(vararg bytes: Int) {
            out.write(bytes.map { it and 0xFF }.toByteArray())
        }

        fun writeText(text: String) {
            out.write(text.toByteArray(charset))
        }

        // Initialize
        write(0x1B, 0x40)

        job.lines.forEach { line ->
            // align
            write(0x1B, 0x61, line.align.escPosValue)

            // bold
            write(0x1B, 0x45, if (line.bold) 1 else 0)

            // size
            val size = if (line.doubleSize) 0x11 else 0x00
            write(0x1D, 0x21, size)

            writeText(line.text)
            write(0x0A)

            // reset
            write(0x1B, 0x45, 0)
            write(0x1D, 0x21, 0x00)
        }

        if (!job.qrContent.isNullOrBlank()) {
            write(0x1B, 0x61, 1) // center

            val qrData = job.qrContent.toByteArray(charset)
            val storeLen = qrData.size + 3
            val pL = storeLen % 256
            val pH = storeLen / 256

            // model 2
            write(0x1D, 0x28, 0x6B, 0x04, 0x00, 0x31, 0x41, 0x32, 0x00)
            // module size
            write(0x1D, 0x28, 0x6B, 0x03, 0x00, 0x31, 0x43, 0x06)
            // error correction
            write(0x1D, 0x28, 0x6B, 0x03, 0x00, 0x31, 0x45, 0x30)
            // store data
            write(0x1D, 0x28, 0x6B, pL, pH, 0x31, 0x50, 0x30)
            out.write(qrData)
            // print qr
            write(0x1D, 0x28, 0x6B, 0x03, 0x00, 0x31, 0x51, 0x30)
            write(0x0A, 0x0A)
        }

        if (job.openCashDrawer) {
            // ESC p m t1 t2
            write(0x1B, 0x70, 0x00, 0x40, 0xF0)
        }

        repeat(job.feedLines) {
            write(0x0A)
        }

        if (job.cut) {
            // GS V B 0
            write(0x1D, 0x56, 0x42, 0x00)
        }

        return out.toByteArray()
    }
}

private fun List<Int>.toByteArray(): ByteArray {
    val result = ByteArray(size)
    forEachIndexed { index, value ->
        result[index] = value.toByte()
    }
    return result
}
