package xyz.pandawa.flutter_veepoo_sdk_android

import android.Manifest
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothManager
import android.bluetooth.le.BluetoothLeScanner
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Handler
import android.os.Looper
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.veepoo.protocol.VPOperateManager
import io.flutter.Log
import io.flutter.plugin.common.EventChannel
import io.flutter.plugin.common.MethodChannel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class BluetoothUtil(
    private val context: Context,
    private val mResult: MethodChannel.Result,
    private val activity: Activity?,
    private val eventSink: EventChannel.EventSink?
) {
    private val bluetoothAdapter: BluetoothAdapter? by lazy {
        val bluetoothManager =
            this.context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager

        bluetoothManager.adapter
    }
    private val bluetoothLeScanner: BluetoothLeScanner? by lazy {
        bluetoothAdapter?.bluetoothLeScanner
    }
    private val scannedDevices = mutableSetOf<String>()
    private var connectHandler: Handler? = null
    private var currentGatt: BluetoothGatt? = null
    private var connectedDeviceAddress: String? = null

    companion object {
        private const val REQUEST_PERMISSIONS: Int = 2
    }

    init {
        VPOperateManager.getInstance().init(this.context)
    }

    fun requestBluetoothPermissions() {
        val permissions = mutableListOf<String>()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            permissions.add(Manifest.permission.BLUETOOTH_SCAN)
            permissions.add(Manifest.permission.BLUETOOTH_CONNECT)
        } else {
            permissions.add(Manifest.permission.BLUETOOTH)
            permissions.add(Manifest.permission.BLUETOOTH_ADMIN)
        }

        val permissionsToRequest = permissions.filter {
            ContextCompat.checkSelfPermission(this.context, it) != PackageManager.PERMISSION_GRANTED
        }

        if (permissionsToRequest.isNotEmpty()) {
            ActivityCompat.requestPermissions(
                activity!!, permissionsToRequest.toTypedArray(), REQUEST_PERMISSIONS
            )
        } else {
            this.mResult.success(null)
        }
    }

    fun startScanDevices() {
        scannedDevices.clear()
        bluetoothLeScanner?.startScan(bleScanCallback)
        Handler(Looper.getMainLooper()).postDelayed({ stopScanDevices() }, 10000)
    }

    fun stopScanDevices() {
        bluetoothLeScanner?.stopScan(bleScanCallback)
    }

    private val bleScanCallback = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult?) {
            super.onScanResult(callbackType, result)
            val device = result?.device

            if (device != null) {
                if (!scannedDevices.contains(device.address)) {
                    scannedDevices.add(device.address)

                    val deviceData = mapOf(
                        "name" to (device.name ?: "Unknown"),
                        "address" to device.address,
                        "rssi" to result.rssi
                    )

                    CoroutineScope(Dispatchers.Main).launch {
                        eventSink?.success(deviceData)
                    }
                }
            }
        }

        override fun onScanFailed(errorCode: Int) {
            super.onScanFailed(errorCode)
            mResult.error(
                "SCAN_FAILED", "Bluetooth LE scan failed with error code $errorCode", null
            )
        }

        override fun onBatchScanResults(results: MutableList<ScanResult>?) {
            super.onBatchScanResults(results)
            results?.forEach {
                onScanResult(
                    ScanSettings.CALLBACK_TYPE_ALL_MATCHES, it
                )
            }
        }
    }

    private val gattCallback = object : BluetoothGattCallback() {
        override fun onConnectionStateChange(gatt: BluetoothGatt?, status: Int, newState: Int) {
            super.onConnectionStateChange(gatt, status, newState)
            connectHandler?.removeCallbacksAndMessages(null)

            if (status == BluetoothGatt.GATT_SUCCESS && newState == BluetoothGatt.STATE_CONNECTED) {
                gatt?.discoverServices()
                Log.i("FlutterVeepooSDK", "Device connected")
            } else {
                connectedDeviceAddress = null
                gatt?.close()
                mResult.error("CONNECTION_FAILED", "Device connection failed", null)
            }
        }
    }

    fun connectDevice(address: String) {
        if (connectedDeviceAddress == address && currentGatt != null) {
            mResult.success("Already connected to $address")
        }

        val device = bluetoothAdapter?.getRemoteDevice(address)

        if (device == null) {
            mResult.error("DEVICE_NOT_FOUND", "Device not found", null)
        }

        connectHandler = Handler(Looper.getMainLooper())
        currentGatt = device?.connectGatt(this.context, false, gattCallback)
        connectedDeviceAddress = address

        connectHandler?.postDelayed(
            {
                currentGatt?.disconnect()
                currentGatt?.close()
                mResult.error("CONNECT_TIMEOUT", "Device connect timeout", null)

            }, 10000L
        )
    }

    fun disconnectDevice() {
        if (currentGatt != null) {
            try {
                currentGatt?.disconnect()
                currentGatt?.close()
                currentGatt = null
                connectedDeviceAddress = null
                mResult.success("Device disconnected")
            } catch (e: Exception) {
                mResult.error("DISCONNECT_FAILED", "Device disconnect failed", e.message)
            }
        } else {
            mResult.error("NOT_CONNECTED", "Device is not connected", null)
        }
    }
}