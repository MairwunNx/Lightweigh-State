package com.mairwunnx.lightweightstate.implementations

/**
 * Lightweight composed state class.
 *
 * @param State state data class type with arguments which has a default values.
 * @param Actions actions sealed class, it's actions what can be performed by you.
 * @param Effects effects sealed class, it's effects which happens in actions, effects can
 * @param state state class instance.
 * @since 1.0.0.
 */
open class LightweightComposedState<State, Actions, Effects>(
    private var state: State
) : BaseComposedStateImpl<State, Actions, Effects>(state) {

    override suspend infix fun performSequentially(actions: Collection<Actions>) = actions.forEach { perform(it) }

    override suspend infix fun perform(action: Actions) {
        if (_interceptor(state, action)) {
            performerExceptionHandlerWrapper(state, action)
        }
    }
}