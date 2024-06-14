package xyz.pandawa.flutter_veepoo_sdk_android

import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.annotation.RequiresApi
import io.flutter.plugin.common.EventChannel
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.PluginRegistry

class MethodChannelHandler(private val context: Context) : MethodChannel.MethodCallHandler,
    PluginRegistry.RequestPermissionsResultListener {

    private var result: MethodChannel.Result? = null
    private lateinit var bluetoothUtil: BluetoothUtil
    private var activity: Activity? = null
    private var eventSink: EventChannel.EventSink? = null

    companion object {
        private const val REQUEST_PERMISSIONS: Int = 2
    }

    fun setActivity(activity: Activity?) {
        this.activity = activity
    }

    fun setEventSink(eventSink: EventChannel.EventSink?) {
        this.eventSink = eventSink
    }

    @RequiresApi(Build.VERSION_CODES.S)
    override fun onMethodCall(call: MethodCall, result: MethodChannel.Result) {
        bluetoothUtil = BluetoothUtil(context, result, activity, eventSink)

        when (call.method) {
            "requestBluetoothPermissions" -> bluetoothUtil.requestBluetoothPermissions()
            "startScanDevices" -> bluetoothUtil.startScanDevices()
            "stopScanDevices" -> bluetoothUtil.stopScanDevices()
            "connectDevice" -> {
                val address = call.argument<String>("address")

                if (address != null) {
                    bluetoothUtil.connectDevice(address)
                } else {
                    result.error("INVALID_ARGUMENT", "Mac address cannot be null", null)
                }
            }
            "disconnectDevice" -> bluetoothUtil.disconnectDevice()
            else -> result.notImplemented()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray
    ): Boolean {
        if (requestCode == REQUEST_PERMISSIONS) {
            if (grantResults.isNotEmpty() && grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
                result?.success(null)
            } else {
                result?.error("PERMISSION_DENIED", "Permission denied", null)
            }
            result = null
            return true
        }
        return false
    }
}