package com.mairwunnx.lightweightstate.implementations

import com.mairwunnx.lightweightstate.ComposedStateConfiguration
import com.mairwunnx.lightweightstate.contracts.ComposedState
import com.mairwunnx.lightweightstate.contracts.Disposable

/**
 * Abstract implementation for lightweight composed states.
 *
 * @param State state data class type with arguments which has a default values.
 * @param Actions actions sealed class, it's actions what can be performed by you.
 * @param Effects effects sealed class, it's effects which happens in actions, effects can
 * @param state state instance.
 * @since 1.0.0.
 */
abstract class BaseComposedStateImpl<State, Actions, Effects>(
    private var state: State
) : ComposedState<State, Actions, Effects>, Disposable {
    private fun notDefined(component: String): Nothing = error("`$component` function is not defined in state")

    protected var _configuration = ComposedStateConfiguration()

    protected var _interceptor: (State, Actions) -> Boolean = { _, _ -> true }
    protected var _performer: suspend (State, Actions) -> Unit = { _, _ -> notDefined("performer") }
    protected var _effector: (State, Effects) -> State = { _, _ -> notDefined("effector") }

    protected val _subscribers = mutableListOf<(State) -> Unit>()

    protected var _catcher0: ((Actions, Exception) -> Unit)? = null
    protected var _catcher1: (State, Actions, Exception) -> State = { state, actions, exception ->
        val message =
            """
                | Exception handler is not assigned, you must set exception handler
                | via calling an `catcher` function in com.mairwunnx.lightweightstate.compose function of your state.
                | 
                | Or configure your state with parameter `ignoreNotAssignedCatcherBlock`
                | and set this value to true.
                | 
                | Exception $exception happened 
                |    in action: ${actions!!::class.java.canonicalName}
                |    while state was: ${state.toString()}
                | 
                | Stacktrace
                |    ${exception.stackTraceToString()}
            """.trimMargin()

        if (_configuration.ignoreNotAssignedCatcherBlock) System.err.println(message) else error(message)
        state
    }

    protected val performerExceptionHandlerWrapper: suspend (State, Actions) -> Unit = { state, actions ->
        runCatching {
            _performer(state, actions)
        }.onFailure {
            if (it !is Exception) throw it
            if (it is IllegalStateException && "function is not defined in state" in (it.message ?: "")) throw it

            if (_catcher0 != null) {
                _catcher0!!(actions, it)
            } else {
                this.state = _catcher1(state, actions, it)
                _subscribers.forEach { it(this.state) }
            }
        }
    }

    /**
     * Does configure composed state with [init].
     *
     * @param init lambda which return new [ComposedStateConfiguration]
     * instance with configured properties.
     * @since 1.0.0.
     */
    fun configure(init: ComposedStateConfiguration.() -> Unit) {
        val configuration = ComposedStateConfiguration()
        configuration.init()
        _configuration = configuration
    }

    /**
     * Does assign interceptor for performer. Any performed action
     * will check via interceptor by state.
     *
     * If interceptor returned false then action will not perform.
     *
     * @param predicate predicate for checking with passed [State] and [Actions].
     * @since 1.0.0.
     */
    fun interceptor(predicate: (State, Actions) -> Boolean) {
        _interceptor = predicate
    }

    /**
     * Does assign performer. Performer have passed [State] and [Actions].
     * In performer for influence on state you must call [effect] function.
     *
     * @param acceptor acceptor accepts [State] and [Actions] and by [Actions]
     * call `effect` (which influence on state) method and do something
     * before or after.
     * @since 1.0.0.
     */
    fun performer(acceptor: suspend (State, Actions) -> Unit) {
        _performer = acceptor
    }

    /**
     * Does assign effector. Effector does affect on state by [Effects] but without
     * business logic, does only copy state. In some cases you can copy state depends
     * on passed in applier [State].
     *
     * @param applier lambda function with passed [State] and [Effects] and requires you
     * return new copied [State] by passed effect.
     * @since 1.0.0.
     */
    fun effector(applier: (State, Effects) -> State) {
        _effector = applier
    }

    /**
     * Does assign catcher. Catcher just catch happened exception in performer block.
     *
     * @param catcher catcher lambda with passed arguments [Actions] and [Exception].
     * @since 1.0.0.
     */
    fun catcher(catcher: (Actions, Exception) -> Unit) {
        _catcher0 = catcher
    }

    /**
     * Does assign extended catcher. Catcher just catch happened exception in performer block.
     * But this catcher with three arguments requires you return [State] in case if exception happened,
     * unlike catcher with two arguments.
     *
     * @param catcher catcher lambda with passed arguments [State], [Actions] and [Exception].
     * @since 1.0.0.
     */
    fun catcher(catcher: (State, Actions, Exception) -> State) {
        _catcher1 = catcher
    }

    /**
     * Subscribes you on changes in this state. You can subscribe more
     * than one time.
     *
     * @param action lambda which accept changed [State].
     * @since 1.0.0.
     */
    infix fun changed(action: (State) -> Unit) {
        _subscribers.add(action)
    }

    override infix fun effect(effect: Effects) {
        val previousState = state
        state = _effector(previousState, effect)
        if (previousState != state || _configuration.notifySameStateChanged) {
            _subscribers.forEach { it(state) }
        }
    }

    override fun dispose() {
        _subscribers.clear()

        _interceptor = { _, _ -> true }
        _performer = { _, _ -> notDefined("performer") }
        _effector = { _, _ -> notDefined("effector") }

        _catcher0 = null
        _catcher1 = { _, _, _ -> notDefined("catcher") }

        _configuration = ComposedStateConfiguration()
    }
}