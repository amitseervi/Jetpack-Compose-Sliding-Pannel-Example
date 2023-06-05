package com.example.designsample

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.designsample.slider.SliderContainerLayout
import com.example.designsample.slider.SliderContainerLayoutV2
import com.example.designsample.ui.theme.DesignSampleTheme

class MainActivity : ComponentActivity() {
    @OptIn(
        ExperimentalFoundationApi::class, ExperimentalAnimationApi::class,
        ExperimentalMaterial3Api::class
    )
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            DesignSampleTheme {
                var bottomBarVisible by remember {
                    mutableStateOf(true)
                }

                var playBarVisible by remember {
                    mutableStateOf(true)
                }
                Box(modifier = Modifier.fillMaxSize()) {
                    SliderContainerLayoutV2(
                        modifier = Modifier.fillMaxSize(),
                        content = {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(it)
                            ) {
                                Column(modifier = Modifier.align(Alignment.Center)) {
                                    Button(onClick = {
                                        bottomBarVisible = false
                                    }, modifier = Modifier) {
                                        Text(text = "Hide BottomBar")
                                    }
                                    Button(onClick = {
                                        bottomBarVisible = true
                                    }, modifier = Modifier) {
                                        Text(text = "Show BottomBar")
                                    }
                                    Button(onClick = {
                                        playBarVisible = false
                                    }, modifier = Modifier) {
                                        Text(text = "Hide PlayBar")
                                    }
                                    Button(onClick = {
                                        playBarVisible = true
                                    }, modifier = Modifier) {
                                        Text(text = "Show PlayBar")
                                    }
                                }
                            }
                        },
                        bottomBar = {
                            BottomAppBar(actions = {
                                IconButton(onClick = { }) {
                                    Icon(Icons.Filled.Home, contentDescription = "Home bottom item")
                                }
                                IconButton(onClick = { }) {
                                    Icon(
                                        Icons.Filled.Search,
                                        contentDescription = "Search bottom item"
                                    )
                                }
                                IconButton(onClick = { }) {
                                    Icon(
                                        Icons.Filled.Place,
                                        contentDescription = "Place bottom item"
                                    )
                                }
                                IconButton(onClick = { }) {
                                    Icon(
                                        Icons.Filled.AccountCircle,
                                        contentDescription = "Account bottom item"
                                    )
                                }
                            })
                        },
                        sliderContent = {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(64.dp)
                                    .background(Color.Red)
                            ) {
                                Text(
                                    text = "Placeholder slider",
                                    modifier = Modifier.align(Alignment.CenterVertically)
                                )
                            }
                        },
                        sliderExpanded = {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(Color.Magenta)
                            ) {

                            }
                        },
                        playBarVisible,
                        bottomBarVisible
                    )
                }

            }
        }
    }
}