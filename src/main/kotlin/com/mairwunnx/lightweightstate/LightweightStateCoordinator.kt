package com.mairwunnx.lightweightstate

import com.mairwunnx.lightweightstate.LightweightStateCoordinator.getOrNull
import com.mairwunnx.lightweightstate.contracts.ComposedState
import com.mairwunnx.lightweightstate.contracts.Disposable
import com.mairwunnx.lightweightstate.implementations.AsyncLightweightComposedState
import com.mairwunnx.lightweightstate.implementations.LightweightComposedState
import java.util.*

/**
 * @return lightweight state coordinator object instance.
 * @since 1.0.0.
 */
fun getStateCoordinator() = LightweightStateCoordinator

/**
 * Lightweight state coordinator, whose responsibility is holds all composed states.
 *
 * You can get your composed state by generic types this and coordinate new composed states.
 *
 * @since 1.0.0.
 */
object LightweightStateCoordinator : Disposable {
    private val holder = mutableMapOf<String, ComposedState<*, *, *>>()

    /**
     * Does coordinate new composed state to map where key is ID and value is composed state instance.
     *
     * @param id ID of composed state for getting state by this ID later. By default, will use random UUID.
     * @param state state to coordinate in coordinator.
     * @since 1.0.0.
     */
    fun coordinate(id: String?, state: ComposedState<*, *, *>) = holder.set(id ?: UUID.randomUUID().toString(), state)

    /**
     * May throw null pointer exception if lightweight composed state not exist with
     * such generic or ID. For more safety getting composed state use [getOrNull].
     *
     * @param T generic type of state class.
     * @param E generic type of actions class.
     * @param R generic type of effects class.
     * @param id ID to get exactly composed state.
     * @return non-nullable lightweight composed state instance by identifiers.
     * @see getOrNull
     * @since 1.0.0.
     */
    fun <T, E, R> get(id: String) = holder[id] as LightweightComposedState<T, E, R>

    /**
     * May throw null pointer exception if lightweight composed state not exist with
     * such generic. For more safety getting composed state use [getOrNull].
     *
     * @param T generic type of state class.
     * @param E generic type of actions class.
     * @param R generic type of effects class.
     * @return non-nullable lightweight composed state instance by identifiers.
     * @see getOrNull
     * @since 1.0.0.
     */
    fun <T, E, R> get(): LightweightComposedState<T, E, R> = holder.values.map {
        runCatching { it as LightweightComposedState<T, E, R> }.getOrNull()
    }.firstOrNull()!!

    /**
     * @param T generic type of state class.
     * @param E generic type of actions class.
     * @param R generic type of effects class.
     * @return nullable lightweight composed state instance by identifiers. Null if
     * lightweight composed state not found with such generic types.
     * @since 1.0.0.
     */
    fun <T, E, R> getOrNull(): LightweightComposedState<T, E, R>? = holder.values.map {
        runCatching { it as LightweightComposedState<T, E, R> }.getOrNull()
    }.firstOrNull()

    /**
     * May throw null pointer exception if async lightweight composed state not exist with
     * such generic or ID. For more safety getting composed state use [getAsyncOrNull].
     *
     * @param T generic type of state class.
     * @param E generic type of actions class.
     * @param R generic type of effects class.
     * @param id ID to get exactly async composed state.
     * @return non-nullable async lightweight composed state instance by identifiers.
     * @see getAsyncOrNull
     * @since 1.0.0.
     */
    @UnstableComposedStateAPI
    fun <T, E, R> getAsync(id: String) = holder[id] as AsyncLightweightComposedState<T, E, R>

    /**
     * May throw null pointer exception if async lightweight composed state not exist with
     * such generic. For more safety getting composed state use [getAsyncOrNull].
     *
     * @param T generic type of state class.
     * @param E generic type of actions class.
     * @param R generic type of effects class.
     * @return non-nullable async lightweight composed state instance by identifiers.
     * @see getAsyncOrNull
     * @since 1.0.0.
     */
    @UnstableComposedStateAPI
    fun <T, E, R> getAsync(): AsyncLightweightComposedState<T, E, R> = holder.values.map {
        runCatching { it as AsyncLightweightComposedState<T, E, R> }.getOrNull()
    }.firstOrNull()!!

    /**
     * @param T generic type of state class.
     * @param E generic type of actions class.
     * @param R generic type of effects class.
     * @return nullable async lightweight composed state instance by identifiers. Null if
     * async lightweight composed state not found with such generic types.
     * @since 1.0.0.
     */
    @UnstableComposedStateAPI
    fun <T, E, R> getAsyncOrNull(): AsyncLightweightComposedState<T, E, R>? = holder.values.map {
        runCatching { it as AsyncLightweightComposedState<T, E, R> }.getOrNull()
    }.firstOrNull()

    override fun dispose() = holder.values.forEach { if (it is Disposable) it.dispose() }
}