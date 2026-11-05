package com.steema.teechart.examples.bitmap

import com.steema.teechart.TeeChart
import java.io.File

/**
 * Bitmap example demonstrating rendering a TeeChart chart to a PNG byte array,
 * then saving it to disk.
 */
fun main() {
    try {
        val img = TeeChart.new()
            .y(2.5, 3.2, 2.8, 4.1)
            .texts("One", "Two", "Three", "Four")
            .style("pie")
            .title("TeeChart for Kotlin")
            .size(800, 500)
            .output("png")
            .renderBytes()

        if (img.isEmpty()) {
            System.err.println("no image bytes returned by the DLL")
            System.exit(1)
        }

        val outFile = File("chart.png")
        outFile.writeBytes(img)
        println("Chart saved to ${outFile.name} (${img.size} bytes)")
    } catch (e: Exception) {
        System.err.println("error: ${e.message}")
        System.exit(1)
    }
}

