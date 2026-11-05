package com.steema.teechart.backend

internal class RenderParams(
    val x: DoubleArray,
    val y: DoubleArray,
    val z: DoubleArray,
    val texts: List<String>,
    val outputType: String,
    val width: Int,
    val height: Int,
    val style: String,
    val title: String,
    val footer: String,
    val horiz: Boolean,
    val commander: Boolean,
    val opengl: Boolean,
    var bufSize: Int = 0
)

internal interface TeeChartBackend {
    fun render(params: RenderParams): Int
    fun getBytes(size: Int): ByteArray
}

