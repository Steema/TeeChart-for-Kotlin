package com.steema.teechart

import java.io.File

/**
 * Locates the native TeeChart library (.dll, .so, or .dylib) according to
 * platform and search order.
 */
internal object LibPath {
    const val ENV_LIB = "TEECHART_LIB"
    const val PROP_LIB = "teechart.lib"

    data class Platform(val dir: String, val file: String)

    fun platformInfo(): Platform {
        val os = System.getProperty("os.name", "").lowercase()
        val arch = System.getProperty("os.arch", "").lowercase()
        val is64 = arch.contains("64")

        return when {
            os.contains("win") -> {
                if (is64) Platform("win64", "teechart.dll")
                else Platform("win32", "teechart.dll")
            }
            os.contains("linux") -> {
                if (is64) Platform("linux64", "libteechart.so")
                else Platform("linux32", "libteechart.so")
            }
            os.contains("mac") || os.contains("darwin") -> {
                Platform("macos", "libteechart.dylib")
            }
            else -> throw UnsupportedOperationException("TeeChart: unsupported platform $os/$arch")
        }
    }

    /**
     * Resolves the native library path.
     *
     * Search order:
     * 1. `TEECHART_LIB` environment variable (file or directory)
     * 2. `teechart.lib` system property (`-Dteechart.lib=...`)
     * 3. Next to the executable / running JAR (and in `libs/<platform>/` below it)
     * 4. `libs/<platform>/` relative to the current working directory
     * 5. Parent directories up to repository root (for running from subprojects/examples)
     */
    fun findLibrary(): String {
        val platform = platformInfo()

        // 1. Explicit environment variable: if set, must be valid
        val env = System.getenv(ENV_LIB)
        if (!env.isNullOrBlank()) {
            val file = resolveFile(env, platform.file)
            if (file.isFile) return file.canonicalPath
            throw IllegalStateException("TeeChart: $ENV_LIB=\"$env\" not found or invalid")
        }

        // 2. Explicit System property
        val prop = System.getProperty(PROP_LIB)
        if (!prop.isNullOrBlank()) {
            val file = resolveFile(prop, platform.file)
            if (file.isFile) return file.canonicalPath
            throw IllegalStateException("TeeChart: -D$PROP_LIB=\"$prop\" not found or invalid")
        }

        val candidates = mutableListOf<File>()

        // 3. Location next to code source / running JAR
        try {
            val codeSource = LibPath::class.java.protectionDomain?.codeSource?.location
            if (codeSource != null) {
                val jarFile = File(codeSource.toURI())
                val baseDir = if (jarFile.isDirectory) jarFile else jarFile.parentFile
                candidates.add(File(baseDir, platform.file))
                candidates.add(File(baseDir, "libs/${platform.dir}/${platform.file}"))
            }
        } catch (_: Exception) {}

        // 4. Current working directory
        val cwd = File(System.getProperty("user.dir", "."))
        candidates.add(File(cwd, platform.file))
        candidates.add(File(cwd, "libs/${platform.dir}/${platform.file}"))

        // 5. Parent directories up to repo root (e.g. running from examples/simple or build/...)
        var parent = cwd.parentFile
        var depth = 0
        while (parent != null && depth < 5) {
            candidates.add(File(parent, "libs/${platform.dir}/${platform.file}"))
            parent = parent.parentFile
            depth++
        }

        for (candidate in candidates) {
            if (candidate.isFile) {
                return candidate.canonicalPath
            }
        }

        val searched = candidates.joinToString("\n  ") { it.absolutePath }
        throw IllegalStateException(
            "TeeChart: native library \"${platform.file}\" not found.\n" +
            "Set $ENV_LIB or place it in libs/${platform.dir}/\nTried:\n  $searched"
        )
    }

    private fun resolveFile(pathStr: String, defaultName: String): File {
        val f = File(pathStr)
        return if (f.isDirectory) File(f, defaultName) else f
    }
}

