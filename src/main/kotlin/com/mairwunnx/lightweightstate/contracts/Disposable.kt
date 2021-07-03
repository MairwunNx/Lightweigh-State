package com.mairwunnx.lightweightstate.contracts

/**
 * Base contract of disposable objects.
 *
 * @since 1.0.0.
 */
interface Disposable {
    /**
     * Does dispose this object. (Cleaning subscribers, handlers, etc.).
     *
     * @since 1.0.0.
     */
    fun dispose()
}