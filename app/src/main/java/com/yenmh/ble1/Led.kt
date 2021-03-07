package com.yenmh.ble1

import android.graphics.Color
import com.yenmh.ble1.LedProfile.GREEN_STRING
import com.yenmh.ble1.LedProfile.RED_STRING

enum class Led(val colorInt: Int) {
    RED(Color.RED), GREEN(Color.GREEN), GRAY(Color.GRAY);

    companion object {
        fun fromMessage(message: String) =
            when (message) {
                RED_STRING -> RED
                GREEN_STRING -> GREEN
                else -> GRAY
            }
    }
}
