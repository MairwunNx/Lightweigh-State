package com.mairwunnx.lightweightstate

import com.mairwunnx.lightweightstate.implementations.AsyncLightweightComposedState
import com.mairwunnx.lightweightstate.implementations.LightweightComposedState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlin.coroutines.CoroutineContext

/**
 * Creates lightweight state composition by three reified types, State, Actions and Effects.
 *
 * @param State state data class type with arguments which has a default values.
 * @param Actions actions sealed class, it's actions what can be performed by you.
 * @param Effects effects sealed class, it's effects which happens in actions, effects can
 * do influence on current state and change it.
 * @param id composition id it's helps you get this composition later from
 * [getStateCoordinator] function.
 * @param builder builder block. In this block will pass [LightweightComposedState]
 * instance.
 * @return [LightweightComposedState] instance.
 * @since 1.0.0.
 */
inline fun <reified State, reified Actions, reified Effects> compose(
    id: String? = null,
    builder: LightweightComposedState<State, Actions, Effects>.() -> Unit
) =
    LightweightComposedState<State, Actions, Effects>(State::class.java.newInstance())
        .apply(builder)
        .also { LightweightStateCoordinator.coordinate(id, it) }

/**
 * Creates ASYNC version of lightweight state composition by three reified types,
 * State, Actions and Effects. All actions will be performed by `launch` coroutines function.
 *
 * @param State state data class type with arguments which has a default values.
 * @param Actions actions sealed class, it's actions what can be performed by you.
 * @param Effects effects sealed class, it's effects which happens in actions, effects can
 * do influence on current state and change it.
 * @param id composition id it's helps you get this composition later from
 * [getStateCoordinator] function.
 * @param scope coroutine scope in which will executed async work.
 * @param dispatcher coroutine dispatcher which will used for execution async work. By default,
 * uses [Dispatchers.Default].
 * @param builder builder block. In this block will pass [AsyncLightweightComposedState]
 * instance.
 * @return [AsyncLightweightComposedState] instance.
 * @since 1.0.0.
 */
@UnstableComposedStateAPI
inline fun <reified State, reified Actions, reified Effects> composeAsync(
    id: String? = null,
    scope: CoroutineScope,
    dispatcher: CoroutineContext = Dispatchers.Default,
    builder: AsyncLightweightComposedState<State, Actions, Effects>.() -> Unit
) =
    AsyncLightweightComposedState<State, Actions, Effects>(State::class.java.newInstance(), scope, dispatcher)
        .apply(builder)
        .also { LightweightStateCoordinator.coordinate(id, it) }