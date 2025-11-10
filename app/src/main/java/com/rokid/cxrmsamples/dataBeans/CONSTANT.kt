package com.rokid.cxrmsamples.dataBeans

import android.annotation.SuppressLint
import android.os.Build

object CONSTANT {
    const val BLUETOOTH_PERMISSION_REQUEST = 0x0010

    @SuppressLint("ObsoleteSdkInt")
    val BLUETOOTH_PERMISSIONS = mutableListOf(
        android.Manifest.permission.BLUETOOTH,
        android.Manifest.permission.BLUETOOTH_ADMIN,
        android.Manifest.permission.ACCESS_COARSE_LOCATION,
        android.Manifest.permission.ACCESS_FINE_LOCATION
    ).apply {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            add(android.Manifest.permission.BLUETOOTH_CONNECT)
            add(android.Manifest.permission.BLUETOOTH_SCAN)
        }
    }.toTypedArray()

    const val SERVICE_UUID = "00009100-0000-1000-8000-00805f9b34fb"
    // Client Secret -- copy from https://ar.rokid.com/ -->Account Center-->Credential information
    const val CLIENT_SECRET = "e5058adf-97a2-11f0-961e-043f72fdb9c8"

    const val CUSTOM_CMD = "Custom CMD"

}