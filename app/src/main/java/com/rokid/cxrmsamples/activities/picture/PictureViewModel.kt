package com.rokid.cxrmsamples.activities.picture

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Size
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.lifecycle.ViewModel
import com.rokid.cxr.client.extend.CxrApi
import com.rokid.cxr.client.extend.callbacks.PhotoResultCallback
import com.rokid.cxr.client.utils.ValueUtil
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class PictureViewModel : ViewModel() {
    private val TAG = "PictureViewModel"

    // [4032x3024, 4000x3000, 4032x2268, 3264x2448, 3200x2400, 2268x3024, 2876x2156, 2688x2016, 2582x1936, 2400x1800, 1800x2400, 2560x1440, 2400x1350, 2048x1536, 2016x1512, 1920x1080, 1600x1200, 1440x1080, 1280x720, 720x1280, 1024x768, 800x600, 648x648, 854x480, 800x480, 640x480, 480x640, 352x288, 320x240, 320x180, 176x144]
    val pictureSize: Array<Size> = arrayOf(
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

    private val _takingPhoto = MutableStateFlow(false)
    val takingPhoto = _takingPhoto.asStateFlow()

    private val _selectedPictureSize = MutableStateFlow(pictureSize[0])
    val selectedPictureSize = _selectedPictureSize.asStateFlow()

    private val _showImageBitmap: MutableStateFlow<ImageBitmap?> = MutableStateFlow(null)
    val showImageBitmap = _showImageBitmap.asStateFlow()

    private val pictureCallback = PhotoResultCallback { status, imageData ->
        _takingPhoto.value = false
        when(status){
            ValueUtil.CxrStatus.RESPONSE_SUCCEED -> {
                // imageData is a webP format image
                val bitmap = BitmapFactory.decodeByteArray(imageData, 0, imageData.size)
                _showImageBitmap.value = bitmap.asImageBitmap()

            }
            else -> {
                _showImageBitmap.value = null
            }
        }
    }

    fun takePicture() {
        val size = _selectedPictureSize.value
        CxrApi.getInstance().takeGlassPhotoGlobal(size.width, size.height, 100, pictureCallback)
        _takingPhoto.value = true
    }

    fun sizeChoose(resolution: Size) {
        _selectedPictureSize.value = resolution
    }

    fun setPhotoParams() {
        CxrApi.getInstance().setPhotoParams(_selectedPictureSize.value.width, _selectedPictureSize.value.height)
    }
}