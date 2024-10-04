package site.shasmatic.flutter_veepoo_sdk_android

import android.app.Activity
import android.content.pm.PackageManager
import android.os.Build
import androidx.annotation.RequiresApi
import com.veepoo.protocol.VPOperateManager
import io.flutter.plugin.common.EventChannel
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.PluginRegistry
import site.shasmatic.flutter_veepoo_sdk_android.utils.BluetoothUtil
import site.shasmatic.flutter_veepoo_sdk_android.utils.DeviceStorageUtil
import site.shasmatic.flutter_veepoo_sdk_android.utils.HeartRateUtil

class MethodChannelHandler(private val vpManager: VPOperateManager,
                           private val deviceStorageUtil: DeviceStorageUtil,
    ) : MethodChannel.MethodCallHandler,
    PluginRegistry.RequestPermissionsResultListener {

    private var result: MethodChannel.Result? = null
    private lateinit var bluetoothUtil: BluetoothUtil
    private lateinit var heartRateUtil: HeartRateUtil
    private var activity: Activity? = null
    private var scanBluetoothEventSink: EventChannel.EventSink? = null
    private var detectHeartEventSink: EventChannel.EventSink? = null

    companion object {
        private const val REQUEST_PERMISSIONS: Int = 2
    }

    fun setActivity(activity: Activity?) {
        this.activity = activity
    }

    fun scanBluetoothEventSink(eventSink: EventChannel.EventSink?) {
        this.scanBluetoothEventSink = eventSink
    }

    fun detectHeartEventSink(eventSink: EventChannel.EventSink?) {
        this.detectHeartEventSink = eventSink
    }

    @RequiresApi(Build.VERSION_CODES.S)
    override fun onMethodCall(call: MethodCall, result: MethodChannel.Result) {
        this.result = result
        bluetoothUtil =
            BluetoothUtil(result, vpManager, scanBluetoothEventSink, activity!!, deviceStorageUtil)
        heartRateUtil = HeartRateUtil(detectHeartEventSink, vpManager, bluetoothUtil)

        when (call.method) {
            "requestBluetoothPermissions" -> bluetoothUtil.requestBluetoothPermissions()
            "scan" -> bluetoothUtil.scan()
            "stop" -> bluetoothUtil.stop()
            "connect" -> {
                val address = call.argument<String>("address")

                if (address != null) {
                    bluetoothUtil.connect(address)
                } else {
                    result.error("INVALID_ARGUMENT", "Mac address cannot be null", null)
                }
            }
            "disconnect" -> bluetoothUtil.disconnect()
            "startDetectHeartAfterBinding" -> {
                val password = call.argument<String>("password")
                val is24H = call.argument<Boolean>("is24H")

                if (password != null && is24H != null) {
                    heartRateUtil.startDetectHeartAfterBinding(password, is24H)
                } else {
                    result.error("INVALID_ARGUMENT", "Password and is24H cannot be null", null)
                }
            }
            "stopDetectHeart" -> heartRateUtil.stopDetectHeart()
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