package com.rokid.cxrmsamples.activities.usageSelection

import android.content.Context
import android.content.Intent
import androidx.lifecycle.ViewModel
import com.rokid.cxrmsamples.activities.audio.AudioUsageActivity
import com.rokid.cxrmsamples.activities.customProtocol.CustomProtocolActivity
import com.rokid.cxrmsamples.activities.customView.CustomViewActivity
import com.rokid.cxrmsamples.activities.deviceInformation.DeviceInformationActivity
import com.rokid.cxrmsamples.activities.mediaFile.MediaFileActivity
import com.rokid.cxrmsamples.activities.picture.PictureActivity
import com.rokid.cxrmsamples.activities.video.VideoActivity
import com.rokid.cxrmsamples.dataBeans.UsageType

class UsageSelectionViewModel: ViewModel() {
    fun toUsage(context: Context, type: UsageType) {
        when(type){
            UsageType.USAGE_TYPE_AUDIO -> {
                context.startActivity(Intent(context, AudioUsageActivity::class.java))
            }
            UsageType.USAGE_TYPE_VIDEO -> {
                context.startActivity(Intent(context, VideoActivity::class.java))
            }
            UsageType.USAGE_TYPE_PHOTO -> {
                context.startActivity(Intent(context, PictureActivity::class.java))
            }
            UsageType.USAGE_TYPE_FILE -> {
                context.startActivity(Intent(context, MediaFileActivity::class.java))
            }
            UsageType.USAGE_TYPE_AI -> TODO()
            UsageType.USAGE_CUSTOM_VIEW -> {
                context.startActivity(Intent(context, CustomViewActivity::class.java))
            }
            UsageType.USAGE_TYPE_CUSTOM_PROTOCOL -> {
                context.startActivity(Intent(context, CustomProtocolActivity::class.java))
            }
            UsageType.USAGE_TYPE_TELEPROMPTER -> TODO()
            UsageType.USAGE_TYPE_TRANSLATION -> TODO()
            UsageType.USAGE_TYPE_DEVICE_INFORMATION -> {
                context.startActivity(Intent(context, DeviceInformationActivity::class.java))
            }
        }
    }

}