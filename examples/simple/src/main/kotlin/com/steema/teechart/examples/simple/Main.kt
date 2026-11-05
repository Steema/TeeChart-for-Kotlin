package com.steema.teechart.examples.simple

import com.steema.teechart.TeeChart

/**
 * Simple example showing basic TeeChart for Kotlin usage:
 * Render a pie chart in a modal GUI window.
 */
fun main() {
    try {
        TeeChart.new()
            .y(2.5, 3.2, 2.8, 4.1)
            .texts("One", "Two", "Three", "Four")
            .style("pie")
            .title("TeeChart for Kotlin")
            .output("modal")
            .size(800, 500)
            .render()

        println("TeeChartRender correctly executed")
    } catch (e: Exception) {
        System.err.println("error: ${e.message}")
        System.exit(1)
    }
}

