package com.steema.teechart

import com.steema.teechart.backend.FfmBackend
import com.steema.teechart.backend.JnaBackend
import com.steema.teechart.backend.RenderParams
import com.steema.teechart.backend.TeeChartBackend

internal object Native {
    @Volatile
    private var backend: TeeChartBackend? = null
    private val lock = Any()

    private fun getBackend(): TeeChartBackend {
        backend?.let { return it }
        synchronized(lock) {
            backend?.let { return it }

            val path = LibPath.findLibrary()
            val b = createBackend(path)
            backend = b
            return b
        }
    }

    private fun createBackend(path: String): TeeChartBackend {
        // 1. Try JNA if present on classpath
        if (isClassAvailable("com.sun.jna.Native")) {
            try {
                return JnaBackend(path)
            } catch (t: Throwable) {
                // If JNA initialization fails, fall through to FFM
            }
        }

        // 2. Try Java 21+ Foreign Function & Memory (FFM) API
        if (isClassAvailable("java.lang.foreign.Linker")) {
            try {
                return FfmBackend(path)
            } catch (t: Throwable) {
                throw IllegalStateException("Failed to initialize FFM backend for $path: ${t.message}", t)
            }
        }

        throw IllegalStateException(
            "No native backend available for TeeChart. " +
            "Please add net.java.dev.jna:jna:5.14.0 dependency or run on Java 21+ with FFM support."
        )
    }

    private fun isClassAvailable(className: String): Boolean {
        return try {
            Class.forName(className, false, Native::class.java.classLoader)
            true
        } catch (_: Throwable) {
            false
        }
    }

    fun render(params: RenderParams): Int {
        val b = getBackend()
        return b.render(params)
    }

    fun getBytes(size: Int): ByteArray {
        val b = getBackend()
        return b.getBytes(size)
    }
}

