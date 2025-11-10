package com.rokid.cxrmsamples.activities.mediaFile

import android.util.Log
import androidx.lifecycle.ViewModel
import com.rokid.cxr.client.extend.CxrApi
import com.rokid.cxr.client.extend.callbacks.SyncStatusCallback
import com.rokid.cxr.client.extend.callbacks.UnsyncNumResultCallback
import com.rokid.cxr.client.extend.callbacks.WifiP2PStatusCallback
import com.rokid.cxr.client.extend.listeners.MediaFilesUpdateListener
import com.rokid.cxr.client.utils.ValueUtil
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.io.File

enum class ConnectionStatus{
    CONNECTED,
    CONNECTING,
    DISCONNECTED
}

class MediaFileViewModel: ViewModel() {

    private val TAG = "MediaFileViewModel"
    private val _connected: MutableStateFlow<ConnectionStatus> = MutableStateFlow(ConnectionStatus.DISCONNECTED)
    val connected = _connected.asStateFlow()

    private val _audioNumber: MutableStateFlow<Int> = MutableStateFlow(0)
    val audioNumber = _audioNumber.asStateFlow()
    private val _pictureNumber: MutableStateFlow<Int> = MutableStateFlow(0)
    val pictureNumber = _pictureNumber.asStateFlow()
    private val _videoNumber: MutableStateFlow<Int> = MutableStateFlow(0)
    val videoNumber = _videoNumber.asStateFlow()

    private val _syncing: MutableStateFlow<Boolean> = MutableStateFlow(false)
    val syncing = _syncing.asStateFlow()

    private val wifiP2PStatusCallback = object : WifiP2PStatusCallback {

        override fun onConnected() {
            _connected.value = ConnectionStatus.CONNECTED
        }

        override fun onDisconnected() {
            _connected.value = ConnectionStatus.DISCONNECTED
        }

        override fun onFailed(p0: ValueUtil.CxrWifiErrorCode?) {
            _connected.value = ConnectionStatus.DISCONNECTED
        }
    }

    private val mediaFilesUpdateListener = MediaFilesUpdateListener { getUnsyncNum() }

    fun setMediaFilesUpdateListener(){
        CxrApi.getInstance().setMediaFilesUpdateListener(mediaFilesUpdateListener)
    }

    private val unsyncNumResultCallback =
        UnsyncNumResultCallback { status, audioNum, pictureNum, videoNum ->
            if (status == ValueUtil.CxrStatus.RESPONSE_SUCCEED){
                _audioNumber.value = audioNum
                _pictureNumber.value = pictureNum
                _videoNumber.value = videoNum
            }
        }

    private val syncStatus = object : SyncStatusCallback{
        override fun onSyncStart() {
            // Todo when sync start
        }

        override fun onSingleFileSynced(p0: String?) {
            // Todo when sync single file
            Log.i(TAG, "sync single file, name = $p0")
        }

        override fun onSyncFailed() {
            _syncing.value = false
            Log.e(TAG, "sync failed")
        }

        override fun onSyncFinished() {
            _syncing.value = false
            Log.i(TAG, "sync finished")
        }

    }

    fun connect(){
        _connected.value = ConnectionStatus.CONNECTING
        CxrApi.getInstance().initWifiP2P(wifiP2PStatusCallback)
    }


    fun disconnect(){
        CxrApi.getInstance().deinitWifiP2P()
        _connected.value = ConnectionStatus.DISCONNECTED
    }

    fun getUnsyncNum(){
        CxrApi.getInstance().getUnsyncNum(unsyncNumResultCallback)
    }

    fun startSync(mediaType: Array<ValueUtil.CxrMediaType>){

        val file = File("/sdcard/Download/Rokid/Media/")
        if (!file.exists()){
            file.mkdirs()
        }

        CxrApi.getInstance().startSync("/sdcard/Download/Rokid/Media/", mediaType, syncStatus)
        _syncing.value = true
    }

    fun stopSync(){
        CxrApi.getInstance().stopSync()
        _syncing.value = false
    }

}