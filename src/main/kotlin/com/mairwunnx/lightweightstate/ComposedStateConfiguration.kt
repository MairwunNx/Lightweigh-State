package com.mairwunnx.lightweightstate

/**
 * Configuration of composed state.
 *
 * @param notifySameStateChanged do notify changing state if it was the same state as previous?
 * @param ignoreNotAssignedCatcherBlock does catcher will ignore unassigned catcher block?
 * @since 1.0.0.
 */
data class ComposedStateConfiguration(
    var notifySameStateChanged: Boolean = false,
    var ignoreNotAssignedCatcherBlock: Boolean = false,
)
