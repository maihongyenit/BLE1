package com.yenmh.ble1.model

import androidx.lifecycle.LiveData
import com.yenmh.ble1.Led

interface BLEPeripheral {

    val ledState: LiveData<Led>

    val bluetoothSupport: LiveData<Boolean>

    fun startGattAdvertising()
}