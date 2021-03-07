package com.yenmh.ble1.extension

import android.bluetooth.BluetoothAdapter
import android.content.Context
import android.content.pm.PackageManager

fun Context.isBluetoothSupport(bluetoothAdapter: BluetoothAdapter?) =
    bluetoothAdapter != null && packageManager.hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)