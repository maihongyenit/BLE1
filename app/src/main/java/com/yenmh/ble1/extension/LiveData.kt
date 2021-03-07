package com.yenmh.ble1.extension

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData

/**
 * Combine this LiveData with another LiveData
 * @param liveData: The LiveData combine with
 * @param transform L1: Value emit from this
 *                  L2: Value emit from liveData
 *                  LiveData<*>: Who invoke this callback
 *                  LiveData<*>: The liveData result
 */
fun <L1, L2, R> LiveData<L1>.combine(
    liveData: LiveData<L2>,
    transform: (L1?, L2?, LiveData<*>, MutableLiveData<R>) -> Unit
): LiveData<R> {
    var value1: L1? = null
    var value2: L2? = null

    val mediatorLiveData = MediatorLiveData<R>()
    mediatorLiveData.addSource(this) {
        value1 = it
        transform(value1, value2, this, mediatorLiveData)
    }
    mediatorLiveData.addSource(liveData) {
        value2 = it
        transform(value1, value2, liveData, mediatorLiveData)
    }
    return mediatorLiveData
}