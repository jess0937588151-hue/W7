package com.example.universalprinter.printer.adapters

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import android.os.RemoteException
import com.example.universalprinter.model.PrintJob
import com.example.universalprinter.printer.EscPosReceiptEncoder
import com.example.universalprinter.printer.Printer
import com.example.universalprinter.printer.PrinterException
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import woyou.aidlservice.jiuiv5.ICallback
import woyou.aidlservice.jiuiv5.IWoyouService

class SunmiPrinter(
    private val context: Context,
    private val charsetName: String
) : Printer {

    private var service: IWoyouService? = null
    private var connection: ServiceConnection? = null
    private var bound = false

    override suspend fun connect() = suspendCancellableCoroutine<Unit> { cont ->
        if (service != null) {
            cont.resume(Unit)
            return@suspendCancellableCoroutine
        }

        val conn = object : ServiceConnection {
            override fun onServiceConnected(name: ComponentName?, binder: IBinder?) {
                service = IWoyouService.Stub.asInterface(binder)
                bound = true
                if (cont.isActive) cont.resume(Unit)
            }

            override fun onServiceDisconnected(name: ComponentName?) {
                service = null
                bound = false
            }
        }

        connection = conn

        val intent = Intent().apply {
            setPackage("woyou.aidlservice.jiuiv5")
            action = "woyou.aidlservice.jiuiv5.IWoyouService"
        }

        val ok = context.bindService(intent, conn, Context.BIND_AUTO_CREATE)
        if (!ok && cont.isActive) {
            cont.resumeWithException(PrinterException("找不到 Sunmi 列印服務"))
        }

        cont.invokeOnCancellation {
            runCatching {
                if (bound && connection != null) {
                    context.unbindService(connection!!)
                }
            }
            bound = false
            service = null
            connection = null
        }
    }

    override suspend fun print(job: PrintJob) = withContext(Dispatchers.IO) {
        val svc = service ?: throw PrinterException("Sunmi 尚未連線")
        val data = EscPosReceiptEncoder.build(job, charsetName)

        suspendCancellableCoroutine<Unit> { cont ->
            val done = AtomicBoolean(false)

            fun success() {
                if (done.compareAndSet(false, true) && cont.isActive) {
                    cont.resume(Unit)
                }
            }

            fun fail(ex: Throwable) {
                if (done.compareAndSet(false, true
