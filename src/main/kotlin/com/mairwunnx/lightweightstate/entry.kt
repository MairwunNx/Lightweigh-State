package com.mairwunnx.lightweightstate

import com.mairwunnx.lightweightstate.Actions.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlin.system.measureTimeMillis

@OptIn(UnstableComposedStateAPI::class)
suspend fun state(scope: CoroutineScope) = composeAsync<State, Actions, Effects>(scope = scope) {
    interceptor { state, actions ->
        when (actions) {
            is Plus -> state.counter < 10
            is Minus -> state.counter > 0
            else -> true
        }
    }

    performer { s, actions ->
        when (actions) {
            is Plus -> {
                delay(3000L)
                effect(Effects.PlusPerformed)
            }
            is Minus -> {
                effect(Effects.MinusPerformed)
            }
            is Refresh -> effect(Effects.RefreshPerformed)
        }
    }

    effector { state, effects ->
        when (effects) {
            is Effects.PlusPerformed -> state.copy(counter = state.counter + 1)
            is Effects.MinusPerformed -> state.copy(counter = state.counter - 1)
            is Effects.RefreshPerformed -> state.copy(counter = 0)
        }
    }
}

data class State(val counter: Int = 0)

sealed class Actions {
    object Plus : Actions()
    object Minus : Actions()
    object Refresh : Actions()
}

sealed class Effects {
    object PlusPerformed : Effects()
    object MinusPerformed : Effects()
    object RefreshPerformed : Effects()
}

@OptIn(UnstableComposedStateAPI::class, kotlin.ExperimentalStdlibApi::class)
suspend fun usage(scope: CoroutineScope) = measureTimeMillis {
    val state = state(scope)

    state changed { println("State changed! $it") }
    state performSequentially buildList { repeat(20) { add(Plus) } }

}.also(::println)

fun main(): Unit = runBlocking {
    usage(this)
    delay(6020)
}
