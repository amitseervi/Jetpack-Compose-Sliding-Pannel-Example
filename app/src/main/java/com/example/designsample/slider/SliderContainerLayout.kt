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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.layout.SubcomposeLayout
import androidx.compose.ui.unit.IntOffset
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

data class MyAnimationState(
    val offset: Offset,
    val alpha: Float,
    val layoutWidth: Int,
    val layoutHeight: Int,
)

@Composable
fun SliderContainerLayout(
    modifier: Modifier = Modifier,
    content: @Composable (padding: PaddingValues) -> Unit,
    bottomBar: @Composable () -> Unit,
    sliderContent: @Composable () -> Unit,
    sliderExpanded: @Composable () -> Unit,
    playBarVisible: Boolean,
    bottomBarVisible: Boolean,
) {

    val playBarAnimation = remember {
        Animatable(0f)
    }
    val bottomBarAnimatedPosition = remember {
        Animatable(0f)
    }
    var isExpanded by remember {
        mutableStateOf(false)
    }
    if (playBarAnimation.value == 0f && !playBarAnimation.isRunning) {
        isExpanded = false
    } else if (playBarAnimation.value == 1f && !playBarAnimation.isRunning) {
        isExpanded = true
    }
    val coroutineScope = rememberCoroutineScope()
    LaunchedEffect(key1 = "handling_bottom_bar_visibility_$bottomBarVisible", block = {
        coroutineScope.launch {
            if (bottomBarVisible) {
                bottomBarAnimatedPosition.animateTo(1f,animationSpec = tween(
                    easing = LinearEasing
                ))
            } else {
                bottomBarAnimatedPosition.animateTo(0f,animationSpec = tween(
                    easing = LinearEasing
                ))
            }
        }
    })
    LaunchedEffect(key1 = "handling_slider_bar_visibility_$playBarVisible", block = {
        coroutineScope.launch {
            if (playBarVisible) {
                playBarAnimation.animateTo(
                    0f, animationSpec = tween(
                        easing = LinearEasing
                    )
                ) // visible but collapsed
            } else {
                playBarAnimation.animateTo(
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
        val playBarVisibleProgress = playBarAnimation.value.coerceIn(0f, 1f)
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
            val bottomBarHeight = ((bottomBarPlaceable.firstOrNull()?.height
                ?: 0) * bottomBarAnimatedPosition.value).roundToInt()
            val sliderContentPlaceable = subcompose(SliderContainerLayoutSlots.SLIDER_CONTENT) {
                Surface(
                    modifier = Modifier
                        .alpha(1 - playBarVisibleProgress)
                        .draggable(
                            orientation = Orientation.Vertical, state = rememberDraggableState(
                                onDelta = {
                                    coroutineScope.launch {
                                        playBarAnimation.snapTo(
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
                                    playBarAnimation.animateTo(
                                        1f, animationSpec = tween(
                                            easing = LinearEasing
                                        )
                                    )
                                } else {
                                    playBarAnimation.animateTo(
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
                    Surface(
                        modifier = Modifier
                            .alpha(playBarVisibleProgress)
                    ) {
                        sliderExpanded()
                    }
                }.map { it.measure(constraints) }
            val sliderHeight = sliderContentPlaceable.firstOrNull()?.height ?: 0
            val sliderTop =
                if (playBarAnimation.value < 0f)
                    interpolateValue(
                        layoutHeight - bottomBarHeight - sliderHeight,
                        layoutHeight - bottomBarHeight,
                        -playBarAnimation.value
                    )
                else interpolateValue(
                    layoutHeight - bottomBarHeight - sliderHeight,
                    0,
                    playBarAnimation.value
                )
            val contentPlaceable = subcompose(SliderContainerLayoutSlots.CONTENT) {
                Surface(
                    modifier = Modifier
                        .alpha(1 - playBarVisibleProgress)
                        .scale((1 - playBarVisibleProgress).coerceIn(0.5f, 1f))
                ) {
                    content(
                        PaddingValues(
                            bottom = (layoutHeight - sliderTop).coerceIn(
                                0,
                                sliderHeight + bottomBarHeight
                            ).toDp()
                        )
                    )
                }

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

private enum class SliderContainerLayoutSlots {
    CONTENT, BOTTOM_BAR, SLIDER_CONTENT, SLIDER_EXPANDED_CONTENT
}