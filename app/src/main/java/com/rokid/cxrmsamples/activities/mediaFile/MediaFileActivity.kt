package com.rokid.cxrmsamples.activities.mediaFile

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.rokid.cxr.client.extend.CxrApi
import com.rokid.cxr.client.utils.ValueUtil
import com.rokid.cxrmsamples.R
import com.rokid.cxrmsamples.ui.theme.CXRMSamplesTheme

class MediaFileActivity : ComponentActivity() {

    private val viewModel: MediaFileViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CXRMSamplesTheme {
                MediaFileScreen(viewModel)
            }
        }

        CxrApi.getInstance().setVideoParams(60, 30, 1920, 1080, 0)
        viewModel.setMediaFilesUpdateListener()


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestPermissions(arrayOf(android.Manifest.permission.NEARBY_WIFI_DEVICES), 1024)
        }
    }
}

@Composable
fun MediaFileScreen(viewModel: MediaFileViewModel) {

    val connectStatus by viewModel.connected.collectAsState()
    val audioNumber by viewModel.audioNumber.collectAsState()
    val pictureNumber by viewModel.pictureNumber.collectAsState()
    val videoNumber by viewModel.videoNumber.collectAsState()
    val syncStatus by viewModel.syncing.collectAsState()

    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(id = R.drawable.glasses_bg),
            modifier = Modifier.fillMaxSize(),
            contentDescription = null,
            alpha = 0.3f
        )
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = "", modifier = Modifier.height(32.dp))

            Button(onClick = { viewModel.getUnsyncNum() }) {
                Text(text = "Get Unsync Numbers")
            }

            Row(modifier = Modifier.fillMaxWidth(0.85f)) {
                Text(text = "Audio: $audioNumber", modifier = Modifier.weight(1f))
                Text(text = "Picture: $pictureNumber", modifier = Modifier.weight(1f))
                Text(text = "Video: $videoNumber", modifier = Modifier.weight(1f))
            }

            Text(
                text = "Connect Status: $connectStatus",
                modifier = Modifier.padding(vertical = 8.dp)
            )
            Row(modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)) {
                Button(onClick = { viewModel.connect() }, modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 4.dp), enabled = connectStatus == ConnectionStatus.DISCONNECTED) {
                    Text(text = "Connect Wi-Fi")
                }

                Button(onClick = { viewModel.disconnect() }, modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 4.dp), enabled = connectStatus == ConnectionStatus.CONNECTED) {
                    Text(text = "DisConnect Wi-Fi")
                }
            }

            Row(modifier = Modifier
                .fillMaxWidth(1f)
                .padding(vertical = 8.dp)) {
                Button(onClick = { viewModel.startSync(arrayOf(ValueUtil.CxrMediaType.VIDEO)) }, modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 4.dp), enabled = !syncStatus) {
                    Text(text = "Sync Videos")
                }
                Button(onClick = { viewModel.startSync(arrayOf(ValueUtil.CxrMediaType.PICTURE)) }, modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 4.dp), enabled = !syncStatus) {
                    Text(text = "Sync Pictures")
                }
                Button(onClick = { viewModel.startSync(arrayOf(ValueUtil.CxrMediaType.AUDIO)) }, modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 4.dp), enabled = !syncStatus) {
                    Text(text = "Sync Audios")
                }

            }

            Button(onClick = { viewModel.stopSync() }, modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp), enabled = syncStatus) {
                Text(text = "Stop Sync")
            }


        }

    }
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    CXRMSamplesTheme {
        MediaFileScreen(viewModel = viewModel { MediaFileViewModel() })
    }
}