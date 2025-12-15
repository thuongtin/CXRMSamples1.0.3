package com.rokid.cxrmsamples.activities.customView

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
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
import androidx.lifecycle.viewmodel.compose.viewModel
import com.rokid.cxrmsamples.R
import com.rokid.cxrmsamples.ui.theme.CXRMSamplesTheme

class CustomViewActivity : ComponentActivity() {

    private val viewModel: CustomViewViewModel by viewModels()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CXRMSamplesTheme {
                CustomViewScreen(
                    viewModel, {
                        viewModel.uploadIcon(this)
                    }
                )
            }
        }
        viewModel.setCustomSceneListener(true)
    }

    override fun onDestroy() {
        viewModel.setCustomSceneListener(false)
        super.onDestroy()
    }
}

@Composable
fun CustomViewScreen(viewModel: CustomViewViewModel, uploadIcons: () -> Unit) {
    val isCustomViewOpen by viewModel.isCustomViewRunning.collectAsState()
    val iconSent by viewModel.iconSent.collectAsState()

    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(id = R.drawable.glasses_bg),
            contentDescription = null,
            alpha = 0.3f,
            modifier = Modifier.fillMaxSize()
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = "Custom page", modifier = Modifier.padding(top = 32.dp, bottom = 8.dp))

            if(!iconSent) {
                Button(
                    onClick = { uploadIcons() },
                    modifier = Modifier.padding(vertical = 8.dp)
                ) {
                    Text("Upload images")
                }
            }else {

                Button(
                    onClick = {
                        viewModel.toggleCustomView()
                    },
                    modifier = Modifier.padding(vertical = 8.dp)
                ) {
                    Text(if (isCustomViewOpen) "Close custom page" else "Open custom page")
                }
                if (isCustomViewOpen) {
                    Button(
                        onClick = { viewModel.updateCustomView() },
                        modifier = Modifier.padding(vertical = 8.dp)
                    ) {
                        Text("Update custom UI")
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    CXRMSamplesTheme {
        CustomViewScreen(viewModel = viewModel { CustomViewViewModel() }, uploadIcons = {})
    }
}
