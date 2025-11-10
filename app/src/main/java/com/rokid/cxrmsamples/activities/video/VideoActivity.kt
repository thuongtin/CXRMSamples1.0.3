package com.rokid.cxrmsamples.activities.video

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
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.rokid.cxrmsamples.R
import com.rokid.cxrmsamples.ui.theme.CXRMSamplesTheme

class VideoActivity : ComponentActivity() {
    private val viewModel: VideoViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CXRMSamplesTheme {
                VideoScreen(
                    viewModel = viewModel
                )
            }
        }
        viewModel.setSceneStatusListener(true)
    }

    override fun onDestroy() {
        viewModel.setSceneStatusListener(false)
        super.onDestroy()
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VideoScreen(viewModel: VideoViewModel) {
    val selectedResolution by viewModel.selectedVideoSize.collectAsState()
    val duration by viewModel.duration.collectAsState()
    val durationUnit by viewModel.durationUnit.collectAsState()
    val isRecording by viewModel.isRecording.collectAsState()

    var resolutionExpanded by remember { mutableStateOf(false) }
    var durationInput by remember { mutableStateOf(duration.toString()) }

    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(id = R.drawable.glasses_bg),
            modifier = Modifier.fillMaxSize(),
            contentDescription = null,
            alpha = 0.3f
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = "视频录制", modifier = Modifier.padding(top = 32.dp, bottom = 16.dp))
            // 分辨率选择下拉框
            ExposedDropdownMenuBox(
                expanded = resolutionExpanded,
                onExpandedChange = { resolutionExpanded = !resolutionExpanded }
            ) {
                TextField(
                    value = "${selectedResolution.width}x${selectedResolution.height}",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("选择分辨率") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = resolutionExpanded) },
                    modifier = Modifier
                        .menuAnchor(MenuAnchorType.PrimaryNotEditable, true)
                        .fillMaxWidth()
                )
                ExposedDropdownMenu(
                    expanded = resolutionExpanded,
                    onDismissRequest = { resolutionExpanded = false }
                ) {
                    viewModel.videoSize.forEach { size ->
                        DropdownMenuItem(
                            text = { Text("${size.width}x${size.height}") },
                            onClick = {
                                viewModel.sizeChoose(size)
                                resolutionExpanded = false
                            }
                        )
                    }
                }
            }

            // 时长输入框
            TextField(
                value = durationInput,
                onValueChange = { input ->
                    durationInput = input
                    val value = input.toIntOrNull()
                    if (value != null) {
                        viewModel.setDuration(value)
                    }
                },
                label = { Text("设置时长") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp)
            )

            // 时间单位单选按钮
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    RadioButton(
                        selected = (durationUnit == VideoViewModel.DurationUnit.SECONDS),
                        onClick = { viewModel.setDurationUnit(VideoViewModel.DurationUnit.SECONDS) }
                    )
                    Text("秒")
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    RadioButton(
                        selected = (durationUnit == VideoViewModel.DurationUnit.MINUTES),
                        onClick = { viewModel.setDurationUnit(VideoViewModel.DurationUnit.MINUTES) }
                    )
                    Text("分")
                }
            }

            // 设置参数按钮
            Button(
                onClick = { viewModel.setVideoParams() },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 24.dp)
            ) {
                Text("设置录像参数")
            }

            // 开始/停止录像按钮
            Button(
                onClick = { viewModel.toggleRecording() },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp)
            ) {
                Text(if (isRecording) "停止录像" else "开始录像")
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    CXRMSamplesTheme {
        VideoScreen(viewModel = androidx.lifecycle.viewmodel.compose.viewModel())
    }
}