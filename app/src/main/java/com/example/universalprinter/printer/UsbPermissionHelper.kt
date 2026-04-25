package com.example.universalprinter.printer

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbManager
import android.os.Build
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
        }

        val receiver = object : BroadcastReceiver() {
            override fun onReceive(ctx: Context?, intent: Intent?) {
                if (intent?.action != ACTION_USB_PERMISSION) return

                runCatching { context.unregisterReceiver(this) }

                val granted = intent.getBooleanExtra(
                    UsbManager.EXTRA_PERMISSION_GRANTED,
                    false
                )
                if (cont.isActive) {
                    cont.resume(granted)
                }
            }
        }

        val flags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PendingIntent.FLAG_IMMUTABLE
        } else {
            0
        }

        val permissionIntent = PendingIntent.getBroadcast(
            context,
            1001,
            Intent(ACTION_USB_PERMISSION),
            flags
        )

        ContextCompat.registerReceiver(
            context,
            receiver,
            IntentFilter(ACTION_USB_PERMISSION),
            ContextCompat.RECEIVER_NOT_EXPORTED
        )

        usbManager.requestPermission(device, permissionIntent)

        cont.invokeOnCancellation {
            runCatching { context.unregisterReceiver(receiver) }
        }
    }
}
package com.example.universalprinter.printer

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbManager
import android.os.Build
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
        }

        val receiver = object : BroadcastReceiver() {
            override fun onReceive(ctx: Context?, intent: Intent?) {
                if (intent?.action != ACTION_USB_PERMISSION) return

                runCatching { context.unregisterReceiver(this) }

                val granted = intent.getBooleanExtra(
                    UsbManager.EXTRA_PERMISSION_GRANTED,
                    false
                )
                if (cont.isActive) {
                    cont.resume(granted)
                }
            }
        }

        val flags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PendingIntent.FLAG_IMMUTABLE
        } else {
            0
        }

        val permissionIntent = PendingIntent.getBroadcast(
            context,
            1001,
            Intent(ACTION_USB_PERMISSION),
            flags
        )

        ContextCompat.registerReceiver(
            context,
            receiver,
            IntentFilter(ACTION_USB_PERMISSION),
            ContextCompat.RECEIVER_NOT_EXPORTED
        )

        usbManager.requestPermission(device, permissionIntent)

        cont.invokeOnCancellation {
            runCatching { context.unregisterReceiver(receiver) }
        }
    }
}
