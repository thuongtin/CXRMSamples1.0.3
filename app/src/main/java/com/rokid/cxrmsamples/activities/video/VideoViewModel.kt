package com.rokid.cxrmsamples.activities.video

import android.util.Size
import androidx.lifecycle.ViewModel
import com.rokid.cxr.client.extend.CxrApi
import com.rokid.cxr.client.extend.listeners.SceneStatusUpdateListener
import com.rokid.cxr.client.utils.ValueUtil
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class VideoViewModel : ViewModel() {
    // 视频分辨率选项
    val videoSize: Array<Size> = arrayOf(
        Size(1920, 1080),
        Size(4032, 3024),
        Size(4000, 3000),
        Size(4032, 2268),
        Size(3264, 2448),
        Size(3200, 2400),
        Size(2268, 3024),
        Size(2876, 2156),
        Size(2688, 2016),
        Size(2582, 1936),
        Size(2400, 1800),
        Size(1800, 2400),
        Size(2560, 1440),
        Size(2400, 1350),
        Size(2048, 1536),
        Size(2016, 1512),
        Size(1600, 1200),
        Size(1440, 1080),
        Size(1280, 720),
        Size(720, 1280),
        Size(1024, 768),
        Size(800, 600),
        Size(648, 648),
        Size(854, 480),
        Size(800, 480),
        Size(640, 480),
        Size(480, 640),
        Size(352, 288),
        Size(320, 240),
        Size(320, 180),
        Size(176, 144)
    )

    private val _selectedVideoSize = MutableStateFlow(videoSize[0])
    val selectedVideoSize = _selectedVideoSize.asStateFlow()

    private val _duration = MutableStateFlow(10)
    val duration = _duration.asStateFlow()

    private val _durationUnit = MutableStateFlow(DurationUnit.SECONDS)
    val durationUnit = _durationUnit.asStateFlow()

    private val _isRecording = MutableStateFlow(false)
    val isRecording = _isRecording.asStateFlow()

    private val sceneStatusUpdateListener = SceneStatusUpdateListener { p0 ->
        p0?.isVideoRecordRunning?.let {
            if (!it) {
                _isRecording.value = false
            }
        }
    }

    fun setSceneStatusListener(toSet: Boolean) {

        CxrApi.getInstance()
            .setSceneStatusUpdateListener(if (toSet) sceneStatusUpdateListener else null)
    }

    fun sizeChoose(resolution: Size) {
        _selectedVideoSize.value = resolution
    }

    fun setDuration(duration: Int) {
        _duration.value = duration
    }

    fun setDurationUnit(unit: DurationUnit) {
        _durationUnit.value = unit
    }

    fun setVideoParams() {
        val size = _selectedVideoSize.value
        CxrApi.getInstance().setVideoParams(
            _duration.value,
            30,
            size.width,
            size.height,
            if (_durationUnit.value == DurationUnit.MINUTES) 0 else 1
        )
    }

    fun toggleRecording() {
        if (_isRecording.value) {
            // 停止录制
            openOrCloseVideoRecord(false)
            _isRecording.value = false
        } else {
            // 开始录制
            openOrCloseVideoRecord(true)
            _isRecording.value = true
        }
    }

    private fun openOrCloseVideoRecord(toRecord: Boolean) {
        if (toRecord) {
            CxrApi.getInstance().controlScene(ValueUtil.CxrSceneType.VIDEO_RECORD, true, null)
        } else {
            CxrApi.getInstance().controlScene(ValueUtil.CxrSceneType.VIDEO_RECORD, false, null)
        }
    }

    enum class DurationUnit {
        SECONDS, MINUTES
    }
}