package dev.aditya.apps_handler

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import io.flutter.plugin.common.EventChannel

class ApplicationChangeListener(private val context: Context) : BroadcastReceiver(), EventChannel.StreamHandler {
    private var eventSink: EventChannel.EventSink? = null

    override fun onListen(arguments: Any?, events: EventChannel.EventSink?) {
        eventSink = events
        val intentFilter = IntentFilter().apply {
            addAction(Intent.ACTION_PACKAGE_ADDED)
            addAction(Intent.ACTION_PACKAGE_REMOVED)
            addAction(Intent.ACTION_PACKAGE_REPLACED)
            addDataScheme("package")
        }
        context.registerReceiver(this, intentFilter)
    }

    override fun onCancel(arguments: Any?) {
        eventSink = null
        try {
            context.unregisterReceiver(this)
        } catch (e: IllegalArgumentException) {
            // Receiver not registered
        }
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent == null || eventSink == null) return

        val packageName = intent.data?.schemeSpecificPart ?: return
        val event = when (intent.action) {
            Intent.ACTION_PACKAGE_ADDED -> "installed"
            Intent.ACTION_PACKAGE_REMOVED -> "uninstalled"
            Intent.ACTION_PACKAGE_REPLACED -> "updated"
            else -> return
        }

        val eventData = mapOf(
            "package_name" to packageName,
            "event" to event
        )
        
        eventSink?.success(eventData)
    }
}
