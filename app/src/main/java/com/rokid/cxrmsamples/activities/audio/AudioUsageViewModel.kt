package com.rokid.cxrmsamples.activities.audio

import android.annotation.SuppressLint
import android.media.AudioFormat
import android.media.AudioTrack
import android.util.Log
import androidx.lifecycle.ViewModel
import com.rokid.cxr.client.extend.CxrApi
import com.rokid.cxr.client.extend.listeners.AudioStreamListener
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.RandomAccessFile

enum class AudioSceneId(val id: Int, val sceneName: String){
    NEAR(0, "Near"),
    FAR(1, "Far"),
    BOTH(2, "Both")
}

enum class PlayState{
    PLAYING, PAUSED, STOPPED
}

class AudioUsageViewModel: ViewModel() {
    private val TAG = "AudioUsageViewModel"

    private val _pickUpType: MutableStateFlow<AudioSceneId> = MutableStateFlow(AudioSceneId.NEAR)
    val pickUpType = _pickUpType.asStateFlow()

    private val _changing: MutableStateFlow<Boolean> = MutableStateFlow(false)
    val changing = _changing.asStateFlow()

    private val _recording: MutableStateFlow<Boolean> = MutableStateFlow(false)
    val recording = _recording.asStateFlow()

    private var recordName = ".pcm"
    private val _listRecordName : MutableStateFlow<List<String>> = MutableStateFlow(emptyList())
    val listRecordName = _listRecordName.asStateFlow()
    
    private var audioTrack: AudioTrack? = null

    private val _playStatus = MutableStateFlow(PlayState.STOPPED)
    val playStatus = _playStatus.asStateFlow()

    // 播放进度相关状态
    private val _currentPosition: MutableStateFlow<Long> = MutableStateFlow(0L)
    val currentPosition = _currentPosition.asStateFlow()
    
    private val _duration: MutableStateFlow<Long> = MutableStateFlow(0L)
    val duration = _duration.asStateFlow()

    private val recordPath = "/sdcard/Download/Rokid/audioRecord"

    private val audioListener = object : AudioStreamListener{
        override fun onStartAudioStream(codeType: Int, streamType: String?) {
            // create a new file to record audio
            recordName = "cxrM_${ System.currentTimeMillis() }.pcm"
            _listRecordName.value += recordName
            Log.i(TAG, "onStartAudioStream: $recordName")
        }

        @SuppressLint("SdCardPath")
        override fun onAudioStream(data: ByteArray?, offset: Int, size: Int) {
            // audio data--存储到应用内
            val file = File(recordPath, recordName)
            val parent = file.parentFile
            if (!parent!!.exists()) {
                Log.e(TAG, "create file error: ${file.absolutePath}")
                parent.mkdirs()
            }
            if (!file.exists()) {
                Log.e(TAG, "create file error: ${file.absolutePath}")
                file.createNewFile()
            }
            val fos = FileOutputStream(file, true)
            data?.let {
                fos.write(it, offset, size)
            }
            fos.close()
        }
    }



    fun startAudioStream(){
        _recording.value = true
        CxrApi.getInstance().setAudioStreamListener(audioListener)
        CxrApi.getInstance().openAudioRecord(1, "audio_stream")
    }

    fun stopAudioStream(){
        CxrApi.getInstance().closeAudioRecord("audio_stream")
        CxrApi.getInstance().setAudioStreamListener(null)
        _recording.value = false
    }

    fun changeAudioSceneId(sceneId: AudioSceneId){
        _changing.value = true
        CxrApi.getInstance().changeAudioSceneId(sceneId.id){id, success ->
            _changing.value = false
            if (success){
                when (id) {
                    0 -> {
                        _pickUpType.value = AudioSceneId.NEAR
                    }
                    1 -> {
                        _pickUpType.value = AudioSceneId.FAR
                    }
                    else -> {
                        _pickUpType.value = AudioSceneId.BOTH
                    }
                }
            }
        }
    }
    
    // 添加播放PCM音频文件的方法
    fun startPlayAudio() {
        Log.d(TAG, "startPlayAudio: $recordName")
        if (_playStatus.value == PlayState.PAUSED){
            // 恢复播放
            audioTrack?.play()
            _playStatus.value = PlayState.PLAYING
            return
        }
        
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val file = File(recordPath, recordName)
                if (!file.exists()) {
                    Log.e(TAG, "Audio file does not exist: ${file.absolutePath}")
                    return@launch
                }
                
                // 计算音频总时长
                val fileSize = file.length()
                val bytesPerSample = 2 // 16Bit = 2 bytes
                val channels = 1 // 单声道
                val sampleRateInHz = 16000 // 16K
                val totalSamples = fileSize / (bytesPerSample * channels)
                val durationMs = (totalSamples * 1000 / sampleRateInHz).toLong()
                
                withContext(Dispatchers.Main) {
                    _duration.value = durationMs
                    _currentPosition.value = 0L
                }
                
                val channelConfig = AudioFormat.CHANNEL_OUT_MONO // 单声道
                val audioFormat = AudioFormat.ENCODING_PCM_16BIT // 16Bit
                
                val bufferSizeInBytes = AudioTrack.getMinBufferSize(sampleRateInHz, channelConfig, audioFormat)
                
                // 使用AudioTrack.Builder替代已废弃的构造函数
                audioTrack = AudioTrack.Builder()
                    .setAudioAttributes(
                        android.media.AudioAttributes.Builder()
                            .setUsage(android.media.AudioAttributes.USAGE_MEDIA)
                            .setContentType(android.media.AudioAttributes.CONTENT_TYPE_MUSIC)
                            .build()
                    )
                    .setAudioFormat(
                        AudioFormat.Builder()
                            .setSampleRate(sampleRateInHz)
                            .setEncoding(audioFormat)
                            .setChannelMask(channelConfig)
                            .build()
                    )
                    .setBufferSizeInBytes(bufferSizeInBytes)
                    .setTransferMode(AudioTrack.MODE_STREAM)
                    .build()

                audioTrack?.play()
                _playStatus.value = PlayState.PLAYING
                
                val dis = RandomAccessFile(file, "r")
                val data = ByteArray(bufferSizeInBytes)
                var totalReadBytes: Long = 0
                
                while (_playStatus.value == PlayState.PLAYING) {
                    var bytesRead = 0
                    while (bytesRead < data.size && dis.getFilePointer() < dis.length()) {
                        data[bytesRead] = dis.readByte()
                        bytesRead++
                    }
                    
                    if (bytesRead > 0) {
                        audioTrack?.write(data, 0, bytesRead)
                        
                        // 更新播放进度
                        totalReadBytes += bytesRead.toLong()
                        val positionMs = (totalReadBytes * 1000 / (sampleRateInHz * bytesPerSample * channels)).toLong()
                        
                        withContext(Dispatchers.Main) {
                            _currentPosition.value = positionMs
                        }
                    }
                    
                    if (dis.getFilePointer() >= dis.length()) {
                        // 文件读取完毕
                        break
                    }
                }
                
                withContext(Dispatchers.Main) {
                    stopPlayAudio()
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error playing audio", e)
                withContext(Dispatchers.Main) {
                    stopPlayAudio()
                }
            }
        }
    }

    fun pausePlayAudio() {
        _playStatus.value = PlayState.PAUSED
        audioTrack?.apply {
            if (playState == AudioTrack.PLAYSTATE_PLAYING) {
                pause()
            }
        }
    }
    
    fun stopPlayAudio() {
        _playStatus.value = PlayState.STOPPED
        audioTrack?.apply {
            if (playState == AudioTrack.PLAYSTATE_PLAYING) {
                stop()
            }
            release()
        }
        audioTrack = null
        Log.d(TAG, "Audio playback stopped")
    }
    
    override fun onCleared() {
        super.onCleared()
        stopPlayAudio()
    }
}