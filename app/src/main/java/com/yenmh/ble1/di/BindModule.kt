package com.yenmh.ble1.di

import com.yenmh.ble1.model.BLEPeripheral
import com.yenmh.ble1.model.imp.BLEPeripheralImp
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
abstract class BindModule {

    @Singleton
    @Binds
    abstract fun bindAppState(impl: BLEPeripheralImp): BLEPeripheral
}