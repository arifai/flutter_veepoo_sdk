package xyz.pandawa.flutter_veepoo_sdk_android

import android.app.Activity
import android.content.Context
import io.flutter.embedding.engine.plugins.FlutterPlugin
import io.flutter.embedding.engine.plugins.activity.ActivityAware
import io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding
import io.flutter.plugin.common.BinaryMessenger
import io.flutter.plugin.common.EventChannel
import io.flutter.plugin.common.MethodChannel

/** FlutterVeepooSdkAndroidPlugin */
class FlutterVeepooSdkAndroidPlugin : FlutterPlugin, ActivityAware {
    private lateinit var channel: MethodChannel
    private lateinit var methodChannelHandler: MethodChannelHandler

    companion object {
        private const val COMMAND_CHANNEL: String = "flutter_veepoo_sdk/command"
        private const val EVENT_CHANNEL: String = "flutter_veepoo_sdk/event"
    }

    override fun onAttachedToEngine(flutterPluginBinding: FlutterPlugin.FlutterPluginBinding) {
        startListening(
            flutterPluginBinding.applicationContext, flutterPluginBinding.binaryMessenger
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

    private fun startListening(appContext: Context, messenger: BinaryMessenger) {
        channel = MethodChannel(messenger, COMMAND_CHANNEL)
        methodChannelHandler = MethodChannelHandler(appContext)

        EventChannel(messenger, EVENT_CHANNEL).setStreamHandler(object :
            EventChannel.StreamHandler {
            override fun onListen(arguments: Any?, events: EventChannel.EventSink) {
                methodChannelHandler.setEventSink(events)
            }

            override fun onCancel(arguments: Any?) {
                methodChannelHandler.setEventSink(null)
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
