package com.example.universalprinter.printer

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbManager
import androidx.core.content.ContextCompat
import kotlin.coroutines.resume
import kotlinx.coroutines.suspendCancellableCoroutine

object UsbPermissionHelper {
    private const val ACTION_USB_PERMISSION =
        "com.example.universalprinter.USB_PERMISSION"

    suspend fun ensurePermission(
        context: Context,
        usbManager: UsbManager,
        device: UsbDevice
    ): Boolean = suspendCancellableCoroutine { cont ->

        if (usbManager.hasPermission(device)) {
            cont.resume(true)
            return@suspendCancellableCoroutine
