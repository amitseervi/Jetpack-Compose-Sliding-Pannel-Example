package com.example.designsample.slider

import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.offset
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
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
    val animatedValue = remember {
        Animatable(0f)
    }
    var isExpanded by remember {
        mutableStateOf(false)
    }
    var isDragging by remember {
        mutableStateOf(false)
    }


    if (animatedValue.value == 0f && !animatedValue.isRunning) {
        isExpanded = false
    } else if (animatedValue.value == 1f && !animatedValue.isRunning) {
        isExpanded = true
    }
    val coroutineScope = rememberCoroutineScope()
    SubcomposeLayout(modifier = modifier) { constraints ->
        val layoutWidth = constraints.maxWidth
        val layoutHeight = constraints.maxHeight
        val looseConstraints = constraints.copy(minWidth = 0, minHeight = 0)
        layout(layoutWidth, layoutHeight) {
            val bottomBarPlaceable = subcompose(SliderContainerLayoutSlots.BOTTOM_BAR) {
                Surface(modifier = Modifier.offset {
                    IntOffset(
                        0,
                        interpolateValue(0, layoutHeight / 4, animatedValue.value)
                    )
                }) {
                    bottomBar()
                }
            }.map {
                it.measure(looseConstraints)
            }
            val bottomBarHeight = bottomBarPlaceable.firstOrNull()?.height ?: 0
            val sliderContentPlaceable = subcompose(SliderContainerLayoutSlots.SLIDER_CONTENT) {
                Surface(
                    modifier = Modifier
                        .alpha(1 - animatedValue.value)
                        .draggable(
                            orientation = Orientation.Vertical, state = rememberDraggableState(
                                onDelta = {
                                    coroutineScope.launch {
                                        animatedValue.snapTo(
                                            (animatedValue.value - (it / (layoutHeight - bottomBarHeight)) * 1.3f).coerceIn(
                                                0f,
                                                1f
                                            )
                                        )
                                    }
                                }
                            ),
                            onDragStopped = {
                                if (animatedValue.value > 0.5f) {
                                    animatedValue.animateTo(1f)
                                } else {
                                    animatedValue.animateTo(0f)

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
                            .alpha(animatedValue.value)
                    ) {
                        sliderExpanded()
                    }
                }.map { it.measure(constraints) }
            val sliderHeight = sliderContentPlaceable.firstOrNull()?.height ?: 0
            val contentPlaceable = subcompose(SliderContainerLayoutSlots.CONTENT) {
                Surface(
                    modifier = Modifier
                        .alpha(1 - animatedValue.value)
                        .scale((1 - animatedValue.value).coerceIn(0.5f, 1f))
                ) {
                    content(PaddingValues(bottom = (bottomBarHeight + sliderHeight).toDp()))
                }

            }.map { it.measure(looseConstraints) }

            contentPlaceable.forEach {
                it.place(0, 0)
            }
            bottomBarPlaceable.forEach {
                it.place(0, layoutHeight - bottomBarHeight)
            }
            val sliderTop =
                interpolateValue(
                    layoutHeight - bottomBarHeight - sliderHeight,
                    0,
                    animatedValue.value
                )
            if (animatedValue.value != 0f) {
                sliderExpandedPlaceable.forEach {
                    it.place(0, sliderTop)
                }
            }
            sliderContentPlaceable.forEach {
                it.place(0, sliderTop)
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