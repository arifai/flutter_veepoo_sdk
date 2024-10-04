package site.shasmatic.flutter_veepoo_sdk_android.utils

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import com.inuker.bluetooth.library.Code.REQUEST_SUCCESS
import com.inuker.bluetooth.library.Constants
import com.inuker.bluetooth.library.search.SearchResult
import com.inuker.bluetooth.library.search.response.SearchResponse
import com.veepoo.protocol.VPOperateManager
import com.veepoo.protocol.listener.base.IABleConnectStatusListener
import com.veepoo.protocol.listener.base.IBleWriteResponse
import com.veepoo.protocol.listener.base.IConnectResponse
import com.veepoo.protocol.listener.base.INotifyResponse
import com.veepoo.protocol.listener.data.ICustomSettingDataListener
import com.veepoo.protocol.listener.data.IDeviceFuctionDataListener
import com.veepoo.protocol.listener.data.IPwdDataListener
import com.veepoo.protocol.listener.data.ISocialMsgDataListener
import com.veepoo.protocol.model.datas.FunctionSocailMsgData
import com.veepoo.protocol.model.enums.EPwdStatus
import io.flutter.plugin.common.EventChannel
import io.flutter.plugin.common.MethodChannel
import site.shasmatic.flutter_veepoo_sdk_android.WriteResponse
import java.lang.reflect.InvocationTargetException

class BluetoothUtil(
    private val mResult: MethodChannel.Result,
    private val vpManager: VPOperateManager,
    eventSink: EventChannel.EventSink?,
    private val activity: Activity,
    private val deviceStorageUtil: DeviceStorageUtil,
) {
    private val tag: String = "BluetoothUtil"
    private var isEnabled: Boolean = true
    private val eventUtil: EventUtil = EventUtil(eventSink)
    private val requestPermissions: Int = 1001
    private val discoveredDevices: MutableSet<String> = mutableSetOf()
    private var isSubmitted: Boolean = false
    private val writeResponse: WriteResponse = WriteResponse()

    @RequiresApi(Build.VERSION_CODES.S)
    fun requestBluetoothPermissions() {
        if (ActivityCompat.checkSelfPermission(activity, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED ||
            ActivityCompat.checkSelfPermission(activity, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED ||
            ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(activity, arrayOf(
                Manifest.permission.BLUETOOTH_SCAN,
                Manifest.permission.BLUETOOTH_CONNECT,
                Manifest.permission.ACCESS_FINE_LOCATION
            ), requestPermissions)
        }
    }

    fun scan() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && isEnabled) {
            Log.i(tag, "Starting scan for devices")
            startScanDevices()
        } else {
            Log.i(tag, "Bluetooth is disabled or scanner not initialized")
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun startScanDevices() {
        try {
            vpManager.startScanDevice(searchResponse)
        } catch (e: InvocationTargetException) {
            e.cause?.printStackTrace()
        } catch (e: Exception) {
            e.cause?.printStackTrace()
        }
    }

    fun connect(address: String) {
        Log.d(tag, "Connecting to device: $address")

        try {
            vpManager.registerConnectStatusListener(address, connectStatusListener)
            vpManager.connectDevice(address, connectResponse, notifyResponse)
        } catch (e: InvocationTargetException) {
            e.cause?.printStackTrace()
        } catch (e: Exception) {
            e.cause?.printStackTrace()
        }
    }

    fun stop() {
        vpManager.stopScanDevice()
        discoveredDevices.clear()
    }

    fun disconnect() {
        vpManager.disconnectWatch(writeResponse)
    }

    fun bindDevice(password: String, is24H: Boolean, onSuccess: () -> Unit, onError: (String) -> Unit) {
        vpManager.confirmDevicePwd(
            confirmPasswordWriteResponse, passwordDataListener(password, is24H, onSuccess, onError),
            deviceFuncDataListener, socialMessageDataListener, customSettingDataListener,
            password, is24H
        )
    }

    private val searchResponse = object : SearchResponse {
        override fun onSearchStarted() {
            Log.d(tag, "Scan Started")
        }

        override fun onDeviceFounded(result: SearchResult?) {
            result?.let {
                Log.d(tag, "Device found: ${it.name} - ${it.address}")

                if (!discoveredDevices.contains(it.address)) {
                    discoveredDevices.add(it.address)

                    val scanResult = mapOf(
                        "name" to (it.name ?: "Unknown"),
                        "address" to it.address,
                        "rssi" to it.rssi,
                    )

                    eventUtil.sendBluetoothEvent(scanResult)
                }
            }
        }

        override fun onSearchStopped() {
            Log.d(tag, "Scan stopped")
        }

        override fun onSearchCanceled() {
            Log.d(tag,"Scan cancelled")
        }
    }

    private val connectResponse = IConnectResponse { state, _, success ->
        Log.d(tag, "Connect state: $state, success: $success")

        if (!isSubmitted) {
            if (success) {
                mResult.success("Device connected")
            } else {
                Log.e(tag, "Device connect failed with state: $state")
                mResult.error("CONNECT_FAILED", "Device connect failed", null)
            }
            isSubmitted = true
        }
    }

    private val notifyResponse = INotifyResponse { state ->
        if (!isSubmitted) {
            if (state == REQUEST_SUCCESS) {
                mResult.success("Notify subscribed")
            } else {
                mResult.error("NOTIFY_FAILED", "Notify failed", null)
            }
            isSubmitted = true
        }
    }

    private val connectStatusListener = object : IABleConnectStatusListener() {
       override fun onConnectStatusChanged(mac: String?, status: Int) {
            if (status == Constants.STATUS_CONNECTED) {
                Log.d(tag, "Status connected")
            } else if (status == Constants.STATUS_DISCONNECTED) {
                Log.d(tag, "Status disconnected")
            }
        }
    }

    private val confirmPasswordWriteResponse = IBleWriteResponse { code ->
            if (code != REQUEST_SUCCESS) {
                mResult.error("BINDING_FAILED", "Binding failed", null)
            }
        }

    private fun passwordDataListener(
        password: String,
        is24H: Boolean,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ): IPwdDataListener {
        return IPwdDataListener { data ->
            Log.d(tag, "Password binding result: $data")
            if (data.getmStatus() == EPwdStatus.CHECK_AND_TIME_SUCCESS) {
                deviceStorageUtil.saveCredentials(password, is24H)
                onSuccess()
            } else if (data.getmStatus() == EPwdStatus.CHECK_FAIL || data.getmStatus() == EPwdStatus.UNKNOW ) {
                onError("Password binding failed")
            }
        }
    }

    private val deviceFuncDataListener = IDeviceFuctionDataListener {
        funcSupport -> Log.d(tag, "Function support data changed: ${funcSupport?.heartDetect}")
    }

    private val socialMessageDataListener = object : ISocialMsgDataListener {
        override fun onSocialMsgSupportDataChange(socialMessageData: FunctionSocailMsgData?) {
            Log.d(tag, "Social message data changed: ${socialMessageData?.msg}")
        }

        override fun onSocialMsgSupportDataChange2(socialMessageData: FunctionSocailMsgData?) {
            Log.d(tag, "Social message data changed: ${socialMessageData?.msg}")
        }
    }

    private val customSettingDataListener = ICustomSettingDataListener {
        setting -> Log.d(tag, "Custom setting data changed: ${setting?.status}")
    }
}