package site.shasmatic.flutter_veepoo_sdk_android

import android.app.Activity
import com.veepoo.protocol.VPOperateManager
import io.flutter.embedding.engine.plugins.FlutterPlugin
import io.flutter.embedding.engine.plugins.activity.ActivityAware
import io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding
import io.flutter.plugin.common.BinaryMessenger
import io.flutter.plugin.common.EventChannel
import io.flutter.plugin.common.MethodChannel
import site.shasmatic.flutter_veepoo_sdk_android.utils.DeviceStorageUtil

/** FlutterVeepooSdkAndroidPlugin */
class FlutterVeepooSdkAndroidPlugin : FlutterPlugin, ActivityAware {
    private lateinit var channel: MethodChannel
    private lateinit var methodChannelHandler: MethodChannelHandler
    private lateinit var deviceStorageUtil: DeviceStorageUtil
    private var vpManager: VPOperateManager? = null

    init {
        vpManager = VPOperateManager.getInstance()
    }

    companion object {
        private const val COMMAND_CHANNEL: String = "flutter_veepoo_sdk/command"
        private const val SCAN_BLUETOOTH_EVENT_CHANNEL: String =
            "flutter_veepoo_sdk/scan_bluetooth_event_channel"
        private const val DETECT_HEART_EVENT_CHANNEL: String =
            "flutter_veepoo_sdk/detect_heart_event_channel"
    }


    override fun onAttachedToEngine(flutterPluginBinding: FlutterPlugin.FlutterPluginBinding) {
        deviceStorageUtil = DeviceStorageUtil(flutterPluginBinding.applicationContext)
        vpManager?.init(flutterPluginBinding.applicationContext)
        startListening(
            flutterPluginBinding.binaryMessenger
        )
    }

    override fun onDetachedFromEngine(binding: FlutterPlugin.FlutterPluginBinding) {
        stopListening()
    }

    override fun onAttachedToActivity(binding: ActivityPluginBinding) {
        startListeningActivity(binding.activity)
    }

    override fun onDetachedFromActivityForConfigChanges() {
        onDetachedFromActivity()
    }

    override fun onReattachedToActivityForConfigChanges(binding: ActivityPluginBinding) {
        onAttachedToActivity(binding)
    }

    override fun onDetachedFromActivity() {
        stopListeningActivity()
    }

    private fun startListening(messenger: BinaryMessenger) {
        channel = MethodChannel(messenger, COMMAND_CHANNEL)
        methodChannelHandler = MethodChannelHandler(vpManager!!, deviceStorageUtil)

        EventChannel(messenger, SCAN_BLUETOOTH_EVENT_CHANNEL).setStreamHandler(object :
            EventChannel.StreamHandler {
            override fun onListen(arguments: Any?, events: EventChannel.EventSink) {
                methodChannelHandler.scanBluetoothEventSink(events)
            }

            override fun onCancel(arguments: Any?) {
                methodChannelHandler.scanBluetoothEventSink(null)
            }
        })

        EventChannel(messenger, DETECT_HEART_EVENT_CHANNEL).setStreamHandler(object :
            EventChannel.StreamHandler {
            override fun onListen(arguments: Any?, events: EventChannel.EventSink) {
                methodChannelHandler.detectHeartEventSink(events)
            }

            override fun onCancel(arguments: Any?) {
                methodChannelHandler.detectHeartEventSink(null)
            }
        })

        channel.setMethodCallHandler(methodChannelHandler)
    }

    private fun stopListening() {
        channel.setMethodCallHandler(null)
    }

    private fun startListeningActivity(activity: Activity) {
        methodChannelHandler.setActivity(activity)
    }

    private fun stopListeningActivity() {
        methodChannelHandler.setActivity(null)
    }
}
