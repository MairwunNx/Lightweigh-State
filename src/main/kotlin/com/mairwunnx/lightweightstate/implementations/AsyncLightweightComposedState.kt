package com.mairwunnx.lightweightstate.implementations

import com.mairwunnx.lightweightstate.UnstableComposedStateAPI
import kotlinx.coroutines.*
import kotlin.coroutines.CoroutineContext

/**
 * Asynchronous lightweight composed state class.
 *
 * @param State state data class type with arguments which has a default values.
 * @param Actions actions sealed class, it's actions what can be performed by you.
 * @param Effects effects sealed class, it's effects which happens in actions, effects can
 * @param state state class instance.
 * @param scope coroutine scope in which will executed async work.
 * @param dispatcher coroutine dispatcher which will used for execution async work. By default,
 * uses [Dispatchers.Default].
 * @since 1.0.0.
 */
@UnstableComposedStateAPI
open class AsyncLightweightComposedState<State, Actions, Effects>(
    private var state: State,
    private val scope: CoroutineScope,
    private val dispatcher: CoroutineContext,
) : BaseComposedStateImpl<State, Actions, Effects>(state) {

    private val jobCanceledMessage = "Perform job was canceled due disposing of composed state"
    private val performQueue = mutableListOf<Job>()
    private var currentJob: Job? = null

    override suspend infix fun performSequentially(actions: Collection<Actions>) = actions.forEach { perform(it) }

    override suspend infix fun perform(action: Actions) {
        performQueue.add(
            scope.launch(dispatcher + coroutineExceptionHandlerOfAction(action)) {
                currentJob = coroutineContext.job
                if (_interceptor(state, action)) {
                    performerExceptionHandlerWrapper(state, action)
                }
            }
        )
    }

    /**
     * Does perform collections of actions sequentially.
     *
     * @param actions collection of actions to perform.
     * @param context coroutine context in which will executed actions.
     * @since 1.0.0.
     */
    suspend fun performSequentially(actions: Collection<Actions>, context: CoroutineContext) =
        actions.forEach { perform(it, context) }

    /**
     * Does simply perform an action.
     *
     * @param action action to perform.
     * @param context coroutine context in which will executed action.
     * @since 1.0.0.
     */
    suspend fun perform(action: Actions, context: CoroutineContext) {
        performQueue.add(
            scope.launch(context + coroutineExceptionHandlerOfAction(action)) {
                currentJob = coroutineContext.job
                if (_interceptor(state, action)) {
                    performerExceptionHandlerWrapper(state, action)
                }
            }
        )
    }

    override fun dispose() {
        super.dispose()
        currentJob?.cancel(jobCanceledMessage)
        performQueue.forEach { it.cancel(jobCanceledMessage) }
    }

    private fun coroutineExceptionHandlerOfAction(action: Actions) =
        CoroutineExceptionHandler { _, it ->
            if (it !is Exception) throw (it)

            if (_catcher0 != null) {
                _catcher0!!(action, it)
            } else {
                this.state = _catcher1(state, action, it)
                _subscribers.forEach { it(this.state) }
            }
        }
}