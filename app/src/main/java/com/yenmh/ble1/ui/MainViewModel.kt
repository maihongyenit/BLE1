package com.yenmh.ble1.ui

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import androidx.lifecycle.ViewModel
import com.yenmh.ble1.model.BLEPeripheral
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(val ble: BLEPeripheral): ViewModel(), LifecycleObserver{

    val ledState = ble.ledState
    val bleSupport = ble.bluetoothSupport

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    fun onStart() {
        ble.startGattAdvertising()
    }
}