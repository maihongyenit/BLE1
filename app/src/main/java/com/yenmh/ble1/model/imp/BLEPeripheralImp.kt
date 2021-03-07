package com.yenmh.ble1.model.imp

import android.bluetooth.*
import android.bluetooth.le.AdvertiseCallback
import android.bluetooth.le.AdvertiseData
import android.bluetooth.le.AdvertiseSettings
import android.bluetooth.le.BluetoothLeAdvertiser
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.ParcelUuid
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.yenmh.ble1.Led
import com.yenmh.ble1.LedProfile.BLE_STRING
import com.yenmh.ble1.LedProfile.MESSAGE_UUID
import com.yenmh.ble1.LedProfile.SERVICE_UUID
import com.yenmh.ble1.extension.combine
import com.yenmh.ble1.extension.isBluetoothSupport
import com.yenmh.ble1.model.BLEPeripheral
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class BLEPeripheralImp @Inject constructor(@ApplicationContext val app: Context) : BLEPeripheral {

    /* Bluetooth API */
    private val bluetoothManager: BluetoothManager by lazy { app.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager }
    private val bluetoothAdapter: BluetoothAdapter by lazy { bluetoothManager.adapter }

    private val advertiser: BluetoothLeAdvertiser by lazy { bluetoothAdapter.bluetoothLeAdvertiser }
    private val advertiseCallback: AdvertiseCallback by lazy { MyAdvertiseCallback() }
    private val advertiseSettings: AdvertiseSettings by lazy { buildAdvertiseSettings() }
    private val advertiseData: AdvertiseData by lazy { buildAdvertiseData() }

    private lateinit var gattServer: BluetoothGattServer
    private val gattServerCallBack: BluetoothGattServerCallback by lazy { MyGattServerCallback() }

    /* State */
    private val _currentDevice = MutableLiveData<BluetoothDevice?>(null)

    private val _ledState = MutableLiveData(Led.GRAY)
    override val ledState: LiveData<Led> =
        _ledState.combine(_currentDevice) { state, device, who, result ->
            when (who) {
                _ledState -> result.value = state!!
                _currentDevice -> if (device == null) result.value = Led.GRAY
            }
        }

    private val _bluetoothSupport = MutableLiveData(app.isBluetoothSupport(bluetoothAdapter))
    override val bluetoothSupport: LiveData<Boolean> = _bluetoothSupport

    private var isStarted = false

    /* Broadcast */
    private val bluetoothReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val blState =
                intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.STATE_OFF)
            when (blState) {
                BluetoothAdapter.STATE_ON -> {
                    isStarted = true
                    startGattServer()
                    startAdvertising()
                }
                BluetoothAdapter.STATE_OFF -> {
                    isStarted = false
                    stopGattServer()
                    stopAdvertising()
                }
            }
        }
    }

    override fun startGattAdvertising() {
        if (_bluetoothSupport.value == true && !isStarted) {
            val filter = IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED)
            app.registerReceiver(bluetoothReceiver, filter)

            if (!bluetoothAdapter.isEnabled) {
                bluetoothAdapter.enable()
            } else {
                isStarted = true
                startGattServer()
                startAdvertising()
            }
        }
    }

    private fun startGattServer() {
        if (!::gattServer.isInitialized) {
            gattServer = bluetoothManager.openGattServer(app, gattServerCallBack)
                .apply { addService(getGattService()) }
        }
    }

    private fun stopGattServer() {
        gattServer.close()
    }

    private fun startAdvertising() {
        advertiser.startAdvertising(advertiseSettings, advertiseData, advertiseCallback)
    }

    private fun stopAdvertising() {
        advertiser.stopAdvertising(advertiseCallback)
    }

    private fun buildAdvertiseSettings() =
        AdvertiseSettings.Builder()
            .setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_LOW_LATENCY)
            .setConnectable(true)
            .setTimeout(0)
            .build()

    private fun buildAdvertiseData() =
        AdvertiseData.Builder()
            .addServiceUuid(ParcelUuid(SERVICE_UUID))
            .setIncludeDeviceName(true)
            .addServiceData(ParcelUuid(SERVICE_UUID), BLE_STRING.toByteArray())
            .build()

    private fun getGattService(): BluetoothGattService {
        val messageCharacteristic = BluetoothGattCharacteristic(
            MESSAGE_UUID,
            BluetoothGattCharacteristic.PROPERTY_WRITE,
            BluetoothGattCharacteristic.PERMISSION_WRITE
        )
        return BluetoothGattService(SERVICE_UUID, BluetoothGattService.SERVICE_TYPE_PRIMARY)
            .apply {
                addCharacteristic(messageCharacteristic)
            }
    }


    inner class MyGattServerCallback : BluetoothGattServerCallback() {
        override fun onConnectionStateChange(device: BluetoothDevice?, status: Int, newState: Int) {
            super.onConnectionStateChange(device, status, newState)
            val isSuccess = status == BluetoothGatt.GATT_SUCCESS
            val isConnected = newState == BluetoothProfile.STATE_CONNECTED
            if (isSuccess && isConnected) {
                _currentDevice.postValue(device)
            } else {
                _currentDevice.postValue(null)
            }
        }

        override fun onCharacteristicWriteRequest(
            device: BluetoothDevice?, requestId: Int, characteristic: BluetoothGattCharacteristic?,
            preparedWrite: Boolean, responseNeeded: Boolean, offset: Int, value: ByteArray?
        ) {
            super.onCharacteristicWriteRequest(
                device,
                requestId,
                characteristic,
                preparedWrite,
                responseNeeded,
                offset,
                value
            )
            if (characteristic != null && characteristic.uuid == MESSAGE_UUID) {
                val message = value?.toString(Charsets.UTF_8)
                message?.let {
                    _ledState.postValue(Led.fromMessage(it))
                }
            }
        }
    }

    inner class MyAdvertiseCallback : AdvertiseCallback() {
        override fun onStartSuccess(settingsInEffect: AdvertiseSettings?) {
            super.onStartSuccess(settingsInEffect)
        }

        override fun onStartFailure(errorCode: Int) {
            super.onStartFailure(errorCode)
            _ledState.postValue(Led.GRAY)
        }
    }
}

