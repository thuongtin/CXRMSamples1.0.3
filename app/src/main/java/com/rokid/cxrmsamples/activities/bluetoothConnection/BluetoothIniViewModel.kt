package com.rokid.cxrmsamples.activities.bluetoothConnection

import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.bluetooth.le.BluetoothLeScanner
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanFilter
import android.bluetooth.le.ScanSettings
import android.content.Context
import android.content.Intent
import android.os.ParcelUuid
import android.util.Log
import androidx.core.content.edit
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.rokid.cxr.client.extend.CxrApi
import com.rokid.cxr.client.extend.callbacks.BluetoothStatusCallback
import com.rokid.cxr.client.utils.ValueUtil
import com.rokid.cxrmsamples.R
import com.rokid.cxrmsamples.activities.usageSelection.UsageSelectionActivity
import com.rokid.cxrmsamples.dataBeans.CONSTANT
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class DeviceItem(
    val device: BluetoothDevice?,
    val name: String,
    val macAddress: String,
    val rssi: Int
)

class BluetoothIniViewModel : ViewModel() {

    private val TAG = "BluetoothIniViewModel"

    private val _recordState: MutableStateFlow<Boolean> = MutableStateFlow(false)
    val recordState: StateFlow<Boolean> = _recordState.asStateFlow()

    private val isScanning: MutableStateFlow<Boolean> = MutableStateFlow(false)
    val isScanningState: StateFlow<Boolean> = isScanning.asStateFlow()

    private val _devicesList: MutableStateFlow<List<DeviceItem>> = MutableStateFlow(emptyList())
    val devicesList: StateFlow<List<DeviceItem>> = _devicesList.asStateFlow()

    private val _recordName: MutableStateFlow<String?> = MutableStateFlow(null)
    val recordName: StateFlow<String?> = _recordName.asStateFlow()

    private val _recordUUID: MutableStateFlow<String?> = MutableStateFlow(null)
    val recordUUID: StateFlow<String?> = _recordUUID.asStateFlow()

    private val _recordMacAddress: MutableStateFlow<String?> = MutableStateFlow(null)
    val recordMacAddress: StateFlow<String?> = _recordMacAddress.asStateFlow()

    private val _connecting: MutableStateFlow<Boolean> = MutableStateFlow(false)
    val connecting: StateFlow<Boolean> = _connecting.asStateFlow()

    private val _connected: MutableStateFlow<Boolean> = MutableStateFlow(false)
    val connected: StateFlow<Boolean> = _connected.asStateFlow()
//    val connected = MutableLiveData<Boolean>()

    val toConnect = MutableLiveData<Boolean>()

    private val connectionState = object : BluetoothStatusCallback {
        override fun onConnectionInfo(
            uuid: String?,
            macAddress: String?,
            p2: String?,
            p3: Int
        ) {
            Log.d(TAG, "onConnectionInfo: uuid=$uuid, macAddress=$macAddress, p2=$p2, p3=$p3")
            if (_recordUUID.value == (uuid ?: "error") && _recordMacAddress.value == (macAddress
                    ?: "error")
            ) {
                Log.d(TAG, "Device matches recorded info")
                // 检查连接状态，如果已连接则不再次尝试连接
                if (!CxrApi.getInstance().isBluetoothConnected) {
                    Log.d(TAG, "Device not connected, posting toConnect")
                    toConnect.postValue(true)
                } else {
                    Log.d(TAG, "Device already connected")
                }
            } else {
                Log.d(TAG, "New device info received")
                uuid?.let { u ->
                    macAddress?.let { m ->
                        Log.d(TAG, "Updating records and posting toConnect")
                        _recordUUID.value = u
                        _recordMacAddress.value = m
                        toConnect.postValue(true)
                    }
                }
            }
        }

        override fun onConnected() {
            Log.d(TAG, "Bluetooth device connected successfully")
            _devicesList.value = emptyList()
            _connected.value = true
            _connecting.value = false
        }

        override fun onDisconnected() {
            Log.d(TAG, "Bluetooth device disconnected")
            _connecting.value = false
            _connected.value = false
        }

        override fun onFailed(p0: ValueUtil.CxrBluetoothErrorCode?) {
            Log.e(TAG, "Bluetooth connection failed with error: $p0")
            _connecting.value = false
            _connected.value = false
        }

    }

    init {
        _recordState.value = false
        // 添加一些示例数据用于测试
//        _devicesList.value = listOf(
//            DeviceItem(null,"Device 1", "00:11:22:33:44:55", -45),
//            DeviceItem(null, "Device 2", "AA:BB:CC:DD:EE:FF", -60),
//            DeviceItem(null, "Device 3", "12:34:56:78:90:AB", -75)
//        )
    }

    private val bleScannerCallback: ScanCallback = object : ScanCallback() {
        @SuppressLint("MissingPermission")
        override fun onScanResult(callbackType: Int, result: android.bluetooth.le.ScanResult) {
            super.onScanResult(callbackType, result)
            val device = result.device
            val name = device.name ?: "Unknown"
            val macAddress = device.address
            val rssi = result.rssi
            Log.d(TAG, "Found BLE device: name=$name, address=$macAddress, rssi=$rssi")
            addDevice(device, rssi)
        }

        override fun onScanFailed(errorCode: Int) {
            Log.e(TAG, "BLE scan failed with error code: $errorCode")
        }
    }

    /**
     * 扫描状态更新
     */
    @SuppressLint("MissingPermission")
    fun handleScan(bleScanner: BluetoothLeScanner?) {
        if (isScanning.value) {
            Log.d(TAG, "Stopping BLE scan")
            bleScanner?.stopScan(bleScannerCallback)
            isScanning.value = false
        } else {
            Log.d(TAG, "Starting BLE scan with service UUID: ${CONSTANT.SERVICE_UUID}")
            val filter =
                ScanFilter.Builder().setServiceUuid(ParcelUuid.fromString(CONSTANT.SERVICE_UUID))
                    .build()
            val scanSettings =
                ScanSettings.Builder().setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY).build()
            bleScanner?.startScan(mutableListOf(filter), scanSettings, bleScannerCallback)
            isScanning.value = true
        }
    }


    /**
     * 检查记录状态
     */
    fun checkRecordState(context: Context) {
        Log.d(TAG, "Checking record state")
        // 从SharedPreference 中读取数据
        val sharedPreferences = context.getSharedPreferences("record", Context.MODE_PRIVATE)
        val recordName = sharedPreferences.getString("record_name", null)
        val recordUUID = sharedPreferences.getString("record_uuid", null)
        val recordMacAddress = sharedPreferences.getString("record_mac_address", null)
        recordName?.let { name ->
            recordUUID?.let { uuid ->
                recordMacAddress?.let { mac ->
                    Log.d(TAG, "Record found: name=$name, uuid=$uuid, mac=$mac")
                    this._recordName.value = name
                    this._recordUUID.value = uuid
                    this._recordMacAddress.value = mac
                    _recordState.value = true
                    _connecting.value = false
                    return
                }
            }
        }
        Log.d(TAG, "No record found")
        _recordState.value = false
    }

    /**
     * 记录设备信息
     */
    fun record(context: Context) {
        Log.d(
            TAG,
            "Recording device info: name=${_recordName.value}, uuid=${_recordUUID.value}, mac=${_recordMacAddress.value}"
        )
        val sharedPreferences = context.getSharedPreferences("record", Context.MODE_PRIVATE)
        sharedPreferences.edit {
            putString("record_name", _recordName.value)
            putString("record_uuid", _recordUUID.value)
            putString("record_mac_address", _recordMacAddress.value)
        }
        _recordState.value = true
    }


    /**
     * 添加设备
     */
    @SuppressLint("MissingPermission")
    fun addDevice(device: BluetoothDevice, rssi: Int) {
        Log.d(
            TAG,
            "Adding device: name=${device.name ?: "Unknown"}, address=${device.address}, rssi=$rssi"
        )
        // 判断设备是否已存在
        val existingDevice = _devicesList.value.find { it.device == device }
        if (existingDevice != null) {
            Log.d(TAG, "Device already exists, updating RSSI")
            updateRssi(device, rssi)
        } else {
            val newDevice = DeviceItem(device, device.name ?: "Unknown", device.address, rssi)
            _devicesList.value = _devicesList.value + newDevice
            Log.d(TAG, "New device added to list, total devices: ${_devicesList.value.size}")
        }
    }

    /**
     * 更新设备信号强度
     */
    fun updateRssi(device: BluetoothDevice, rssi: Int) {
        Log.d(TAG, "Updating RSSI for device ${device.address}: $rssi")
        _devicesList.value = _devicesList.value.map {
            if (it.device == device) {
                it.copy(rssi = rssi)
            } else {
                it
            }
        }
    }

    /**
     * 清空设备列表
     */
    fun clearDevices() {
        Log.d(TAG, "Clearing device list")
        _devicesList.value = emptyList()
    }

    /**
     * 连接蓝牙Socket
     */
    fun connectBTSocket(context: Context) {
        Log.d(
            TAG,
            "Reconnecting to device: uuid=${_recordUUID.value}, mac=${_recordMacAddress.value}"
        )
        // 重新连接设备
        CxrApi.getInstance().connectBluetooth(
            context,
            _recordUUID.value ?: "error",
            _recordMacAddress.value ?: "error",
            connectionState,
            readRawFile(context),
            CONSTANT.CLIENT_SECRET.replace("-", "")
        )
    }


    /**
     * 设备点击处理
     */
    fun deviceClicked(activity: Context, deviceItem: DeviceItem?) {
        deviceItem?.let {
            Log.d(TAG, "Device clicked: name=${it.name}, address=${it.macAddress}")
            _recordName.value = it.name
            CxrApi.getInstance().initBluetooth(activity, it.device, connectionState)
            _connecting.value = true
        }
    }

    /**
     * 读取raw目录下的.lc 文件
     */
    fun readRawFile(context: Context): ByteArray {
        Log.d(TAG, "Reading raw file: sn_6101e2e5ec2f4c5abadc3597066af969")
        val inputStream =
            context.resources.openRawResource(R.raw.sn_6101e2e5ec2f4c5abadc3597066af969)
        val bytes = inputStream.readBytes()
        Log.d(TAG, "Read ${bytes.size} bytes from raw file")
        return bytes
    }

    fun disconnect() {
        CxrApi.getInstance().deinitBluetooth()
    }

    fun toUseGlasses(context: Context){
        context.startActivity(Intent(context, UsageSelectionActivity::class.java))
    }

    fun checkConnection() {
        _connected.value = CxrApi.getInstance().isBluetoothConnected
    }
}