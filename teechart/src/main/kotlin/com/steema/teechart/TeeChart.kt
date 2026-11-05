package com.steema.teechart

/**
 * Entry point object and factory functions for creating TeeChart instances in Kotlin.
 */
object TeeChart {
    /** Returns a new [Chart] instance with default settings. */
    @JvmStatic
    fun new(): Chart = Chart()

    /** Alias for [new]. */
    @JvmStatic
    fun chart(): Chart = Chart()

    /** Retrieves the last rendered image bytes with the specified byte count. */
    @JvmStatic
    fun getBytes(size: Int): ByteArray = Native.getBytes(size)
}

/** Top-level Kotlin DSL / factory function to create a new [Chart]. */
fun teechart(): Chart = Chart()

/** Top-level Kotlin DSL / factory function to configure a new [Chart] with a block. */
inline fun teechart(configure: Chart.() -> Unit): Chart = Chart().apply(configure)

