package com.example.designsample.slider

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.offset
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.layout.SubcomposeLayout
import androidx.compose.ui.unit.IntOffset
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@Composable
fun SliderContainerLayoutV2(
    modifier: Modifier = Modifier,
    content: @Composable (padding: PaddingValues) -> Unit,
    bottomBar: @Composable () -> Unit,
    sliderContent: @Composable () -> Unit,
    sliderExpanded: @Composable () -> Unit,
    slidingBottomBarVisible: Boolean,
    navigationBottomBarVisible: Boolean,
) {

    val slidingBarAnimation = remember {
        Animatable(0f)
    }
    val navigationBarAnimation = remember {
        Animatable(0f)
    }
    var isExpanded by remember {
        mutableStateOf(false)
    }
    if (slidingBarAnimation.value == 0f && !slidingBarAnimation.isRunning) {
        isExpanded = false
    } else if (slidingBarAnimation.value == 1f && !slidingBarAnimation.isRunning) {
        isExpanded = true
    }
    val coroutineScope = rememberCoroutineScope()
    LaunchedEffect(key1 = "handling_bottom_bar_visibility_$navigationBottomBarVisible", block = {
        coroutineScope.launch {
            if (navigationBottomBarVisible) {
                navigationBarAnimation.animateTo(
                    1f, animationSpec = tween(
                        easing = LinearEasing
                    )
                )
            } else {
                navigationBarAnimation.animateTo(
                    0f, animationSpec = tween(
                        easing = LinearEasing
                    )
                )
            }
        }
    })
    LaunchedEffect(key1 = "handling_slider_bar_visibility_$slidingBottomBarVisible", block = {
        coroutineScope.launch {
            if (slidingBottomBarVisible) {
                slidingBarAnimation.animateTo(
                    0f, animationSpec = tween(
                        easing = LinearEasing
                    )
                ) // visible but collapsed
            } else {
                slidingBarAnimation.animateTo(
                    -1f, animationSpec = tween(
                        easing = LinearEasing
                    )
                ) //not visible
            }
        }
    })

    SubcomposeLayout(modifier = modifier) { constraints ->
        val layoutWidth = constraints.maxWidth
        val layoutHeight = constraints.maxHeight
        val looseConstraints = constraints.copy(minWidth = 0, minHeight = 0)
        val playBarVisibleProgress = slidingBarAnimation.value.coerceIn(0f, 1f)
        layout(layoutWidth, layoutHeight) {
            val bottomBarPlaceable = subcompose(SliderContainerLayoutSlots.BOTTOM_BAR) {
                Surface(modifier = Modifier.offset {
                    IntOffset(
                        0,
                        interpolateValue(0, layoutHeight / 4, playBarVisibleProgress)
                    )
                }) {
                    bottomBar()
                }
            }.map {
                it.measure(looseConstraints)
            }
            val bottomBarFixedHeight = bottomBarPlaceable.firstOrNull()?.height ?: 0
            val bottomBarHeight = (bottomBarFixedHeight * navigationBarAnimation.value).roundToInt()
            val sliderContentPlaceable = subcompose(SliderContainerLayoutSlots.SLIDER_CONTENT) {
                Surface(
                    modifier = Modifier
                        .alpha(1 - playBarVisibleProgress)
                        .draggable(
                            orientation = Orientation.Vertical, state = rememberDraggableState(
                                onDelta = {
                                    coroutineScope.launch {
                                        slidingBarAnimation.snapTo(
                                            (playBarVisibleProgress - (it / (layoutHeight - bottomBarHeight)) * 1.3f).coerceIn(
                                                0f,
                                                1f
                                            )
                                        )
                                    }
                                }
                            ),
                            onDragStopped = {
                                if (playBarVisibleProgress > 0.5f) {
                                    slidingBarAnimation.animateTo(
                                        1f, animationSpec = tween(
                                            easing = LinearEasing
                                        )
                                    )
                                } else {
                                    slidingBarAnimation.animateTo(
                                        0f, animationSpec = tween(
                                            easing = LinearEasing
                                        )
                                    )

                                }
                            }
                        )
                ) {
                    sliderContent()
                }

            }.map {
                it.measure(looseConstraints)
            }
            val sliderExpandedPlaceable =
                subcompose(SliderContainerLayoutSlots.SLIDER_EXPANDED_CONTENT) {
                    Surface(modifier = Modifier) {
                        sliderExpanded()
                    }
                }.map { it.measure(constraints) }
            val sliderHeight = sliderContentPlaceable.firstOrNull()?.height ?: 0
            val sliderTop =
                if (slidingBarAnimation.value < 0f)
                    interpolateValue(
                        layoutHeight - bottomBarHeight - sliderHeight,
                        layoutHeight - bottomBarHeight,
                        -slidingBarAnimation.value
                    )
                else interpolateValue(
                    layoutHeight - bottomBarHeight - sliderHeight,
                    0,
                    slidingBarAnimation.value
                )
            val contentPlaceable = subcompose(SliderContainerLayoutSlots.CONTENT) {
                content(
                    PaddingValues(
                        bottom = (layoutHeight - sliderTop).coerceIn(
                            0,
                            sliderHeight + bottomBarFixedHeight
                        ).toDp()
                    )
                )
            }.map { it.measure(looseConstraints) }

            contentPlaceable.forEach {
                it.place(0, 0)
            }
            // -1 = (layoutHeight - bottomBarHeight)
            // 1 = (layoutHeight - bottomBarHeight - playBarHeight)
            // 0 = 0f
            if (playBarVisibleProgress != 0f) {
                sliderExpandedPlaceable.forEach {
                    it.place(0, sliderTop)
                }
            }
            sliderContentPlaceable.forEach {
                it.place(0, sliderTop)
            }
            bottomBarPlaceable.forEach {
                it.place(
                    0,
                    layoutHeight - bottomBarHeight
                )
            }
        }
    }
}

private fun interpolateValue(start: Int, end: Int, progress: Float): Int {
    return ((end - start).times(progress).roundToInt() + start)
}