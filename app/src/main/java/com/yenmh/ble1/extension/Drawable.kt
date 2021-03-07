package com.yenmh.ble1.extension

import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.ShapeDrawable

fun Drawable.changeColor(colorInt: Int) {
    when (this) {
        is GradientDrawable -> setColor(colorInt)
        is ShapeDrawable -> paint.color = colorInt
        is ColorDrawable -> color = colorInt
    }
}