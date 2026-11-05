package com.steema.teechart

import com.steema.teechart.backend.RenderParams

/**
 * Fluent builder for creating and rendering TeeChart charts using the native library.
 *
 * Example:
 * ```kotlin
 * TeeChart.new()
 *     .y(2.5, 3.2, 2.8, 4.1)
 *     .texts("One", "Two", "Three", "Four")
 *     .style("pie")
 *     .title("TeeChart for Kotlin")
 *     .output("modal")
 *     .size(800, 500)
 *     .render()
 * ```
 */
class Chart {
    private var xValues: DoubleArray = DoubleArray(0)
    private var yValues: DoubleArray = DoubleArray(0)
    private var zValues: DoubleArray = DoubleArray(0)
    private var textLabels: List<String> = emptyList()
    private var chartStyle: String = ""
    private var chartTitle: String = ""
    private var chartFooter: String = ""
    private var chartWidth: Int = 0
    private var chartHeight: Int = 0
    private var chartOutput: String = ""
    private var isHoriz: Boolean = false
    private var showCommander: Boolean = false
    private var useOpenGL: Boolean = false
    private var lastBufferSize: Int = 0

    // Fluent setters (idiomatic lowercase)

    /** Sets the X values. */
    fun x(vararg values: Double): Chart = apply { xValues = values.clone() }
    fun x(values: DoubleArray): Chart = apply { xValues = values.clone() }
    fun x(values: List<Double>): Chart = apply { xValues = values.toDoubleArray() }

    /** Sets the Y values. */
    fun y(vararg values: Double): Chart = apply { yValues = values.clone() }
    fun y(values: DoubleArray): Chart = apply { yValues = values.clone() }
    fun y(values: List<Double>): Chart = apply { yValues = values.toDoubleArray() }

    /** Sets the Z values (for 3D series). */
    fun z(vararg values: Double): Chart = apply { zValues = values.clone() }
    fun z(values: DoubleArray): Chart = apply { zValues = values.clone() }
    fun z(values: List<Double>): Chart = apply { zValues = values.toDoubleArray() }

    /** Sets point text labels. */
    fun texts(vararg values: String): Chart = apply { textLabels = values.toList() }
    fun texts(values: List<String>): Chart = apply { textLabels = values.toList() }

    /** Sets the chart series style, e.g. "pie", "bar", "line", "area". */
    fun style(s: String): Chart = apply { chartStyle = s }

    /** Sets the chart title. */
    fun title(s: String): Chart = apply { chartTitle = s }

    /** Sets the chart footer. */
    fun footer(s: String): Chart = apply { chartFooter = s }

    /** Sets the output size in pixels (width, height). 0 lets the library decide. */
    fun size(width: Int, height: Int): Chart = apply {
        chartWidth = width
        chartHeight = height
    }

    /**
     * Sets the output type.
     * Use "modal" or "window" for GUI displays, or "png" / "jpeg" for image output.
     */
    fun output(s: String): Chart = apply { chartOutput = s }

    /** Draws the series horizontally (e.g. horizontal bar series). */
    fun horizontal(b: Boolean = true): Chart = apply { isHoriz = b }

    /** Shows the chart commander/editor toolbar. */
    fun commander(b: Boolean = true): Chart = apply { showCommander = b }

    /** Enables OpenGL hardware acceleration. */
    fun openGL(b: Boolean = true): Chart = apply { useOpenGL = b }

    // Go-style uppercase aliases for seamless cross-language consistency
    fun X(vararg values: Double): Chart = x(*values)
    fun Y(vararg values: Double): Chart = y(*values)
    fun Z(vararg values: Double): Chart = z(*values)
    fun Texts(vararg values: String): Chart = texts(*values)
    fun Style(s: String): Chart = style(s)
    fun Title(s: String): Chart = title(s)
    fun Footer(s: String): Chart = footer(s)
    fun Size(width: Int, height: Int): Chart = size(width, height)
    fun Output(s: String): Chart = output(s)
    fun Horizontal(b: Boolean = true): Chart = horizontal(b)
    fun Commander(b: Boolean = true): Chart = commander(b)
    fun OpenGL(b: Boolean = true): Chart = openGL(b)

    /**
     * Renders the chart using the native TeeChart library.
     * When output is "modal" or "window", this call blocks until the window closes.
     */
    fun render(): Chart {
        val params = RenderParams(
            x = xValues,
            y = yValues,
            z = zValues,
            texts = textLabels,
            outputType = chartOutput,
            width = chartWidth,
            height = chartHeight,
            style = chartStyle,
            title = chartTitle,
            footer = chartFooter,
            horiz = isHoriz,
            commander = showCommander,
            opengl = useOpenGL,
            bufSize = 0
        )

        val rc = Native.render(params)
        if (rc != 0) {
            throw RuntimeException("TeeChart render failed with error code $rc")
        }
        lastBufferSize = params.bufSize
        return this
    }

    /**
     * Renders the chart to memory and returns the encoded image bytes (PNG or JPEG).
     * Automatically sets output to "png" if not already set to an image format.
     */
    fun renderBytes(): ByteArray {
        if (!chartOutput.equals("png", ignoreCase = true) &&
            !chartOutput.equals("jpeg", ignoreCase = true) &&
            !chartOutput.equals("jpg", ignoreCase = true)) {
            chartOutput = "png"
        }
        render()
        if (lastBufferSize <= 0) {
            return ByteArray(0)
        }
        return Native.getBytes(lastBufferSize)
    }
}

