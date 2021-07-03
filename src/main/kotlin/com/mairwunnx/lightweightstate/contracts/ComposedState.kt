package com.mairwunnx.lightweightstate.contracts

/**
 * Base contract of all lightweight composed states implementations.
 *
 * @param State state data class type with arguments which has a default values.
 * @param Actions actions sealed class, it's actions what can be performed by you.
 * @param Effects effects sealed class, it's effects which happens in actions, effects can
 * @since 1.0.0.
 */
interface ComposedState<State, Actions, Effects> {
    /**
     * Does simply perform an action.
     *
     * @param action action to perform.
     * @since 1.0.0.
     */
    suspend infix fun perform(action: Actions)

    /**
     * Does perform collections of actions sequentially.
     *
     * @param actions collection of actions to perform.
     * @since 1.0.0.
     */
    suspend infix fun performSequentially(actions: Collection<Actions>)

    /**
     * Produces effect which affects on state.
     * @param effect effect to produce.
     * @since 1.0.0.
     */
    infix fun effect(effect: Effects)
}