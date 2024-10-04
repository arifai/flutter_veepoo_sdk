package site.shasmatic.flutter_veepoo_sdk_android.utils

import io.flutter.plugin.common.EventChannel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class EventUtil(private val eventSink: EventChannel.EventSink?) {
    fun sendBluetoothEvent(scanResult: Map<String, Any>) {
        CoroutineScope(Dispatchers.Main).launch {
            eventSink?.success(scanResult)
        }
    }

    fun sendHeartRateEvent(heartRate: Map<String, Any>) {
        CoroutineScope(Dispatchers.Main).launch {
            eventSink?.success(heartRate)
        }
    }
}