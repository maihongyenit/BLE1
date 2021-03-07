package com.yenmh.ble1.databinding

import android.view.View
import androidx.databinding.BindingAdapter
import com.yenmh.ble1.Led
import com.yenmh.ble1.extension.changeColor

@BindingAdapter("ledState")
fun ledState(
    view: View,
    ledState: Led,
) {
    val background = view.background
    background.changeColor(ledState.colorInt)
}

@BindingAdapter("myVisible")
fun myVisible(
    view: View,
    visible: Boolean,
) {
    if (!visible) {
        view.visibility = View.GONE
    } else {
        view.visibility = View.VISIBLE
    }
}