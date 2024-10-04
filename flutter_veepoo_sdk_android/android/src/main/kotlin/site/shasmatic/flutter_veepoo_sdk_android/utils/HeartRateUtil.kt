package site.shasmatic.flutter_veepoo_sdk_android.utils

import android.util.Log
import com.veepoo.protocol.VPOperateManager
import io.flutter.plugin.common.EventChannel
import site.shasmatic.flutter_veepoo_sdk_android.WriteResponse

class HeartRateUtil(
    eventSink: EventChannel.EventSink?,
    private val vpManager: VPOperateManager,
    private val bluetoothUtil: BluetoothUtil
) {
    private val tag: String = "HeartRateUtil"
    private val eventUtil: EventUtil = EventUtil(eventSink)
    private val writeResponse: WriteResponse = WriteResponse()

    private fun startDetectHeart() {
        try {
            vpManager.startDetectHeart(writeResponse) { data ->
                val heartDataMap = mapOf("data" to data.data, "status" to data.heartStatus.name)

                eventUtil.sendHeartRateEvent(heartDataMap)
            }
        } catch (e: Exception) {
            Log.e(tag, "Error starting heart rate detection: ${e.message}")
        }
    }

    fun stopDetectHeart() {
        vpManager.stopDetectHeart(writeResponse)
    }

    fun startDetectHeartAfterBinding(password: String, is24H: Boolean) {
        bluetoothUtil.bindDevice(password, is24H, onSuccess = {
            Log.d(tag, "Device bound successfully, starting heart rate detection")
            startDetectHeart()
        }, onError = {message ->
            Log.e(tag, "Device binding failed: $message")
        })
    }
}