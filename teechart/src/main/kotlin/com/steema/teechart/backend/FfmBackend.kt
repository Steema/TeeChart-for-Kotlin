package com.steema.teechart.backend

import java.lang.foreign.*
import java.lang.invoke.MethodHandle
import java.nio.charset.StandardCharsets

/**
 * Project Panama / Foreign Function & Memory (FFM) backend.
 * Zero-dependency native bridge for modern JDKs (Java 21+).
 */
internal class FfmBackend(libPath: String) : TeeChartBackend {
    private val renderHandle: MethodHandle
    private val getBytesHandle: MethodHandle

    init {
        System.load(libPath)
        val linker = Linker.nativeLinker()
        val lookup = SymbolLookup.loaderLookup()

        val renderSymbol = lookup.find("TeeChartRender")
            .orElseThrow { IllegalStateException("Cannot find TeeChartRender symbol in $libPath") }
        val getBytesSymbol = lookup.find("TeeChartGetBytes")
            .orElseThrow { IllegalStateException("Cannot find TeeChartGetBytes symbol in $libPath") }

        val renderDesc = FunctionDescriptor.of(
            ValueLayout.JAVA_INT,
            ValueLayout.ADDRESS, ValueLayout.ADDRESS, // nx, x
            ValueLayout.ADDRESS, ValueLayout.ADDRESS, // ny, y
            ValueLayout.ADDRESS, ValueLayout.ADDRESS, // nz, z
            ValueLayout.ADDRESS, ValueLayout.ADDRESS, // ntexts, texts
            ValueLayout.ADDRESS,                      // output_type
            ValueLayout.ADDRESS, ValueLayout.ADDRESS, // width, height
            ValueLayout.ADDRESS, ValueLayout.ADDRESS, ValueLayout.ADDRESS, // style, title, footer
            ValueLayout.ADDRESS, ValueLayout.ADDRESS, ValueLayout.ADDRESS, // horiz, cmd, gl
            ValueLayout.ADDRESS                       // bufSize
        )
        renderHandle = linker.downcallHandle(renderSymbol, renderDesc)

        val getBytesDesc = FunctionDescriptor.ofVoid(
            ValueLayout.ADDRESS, ValueLayout.ADDRESS
        )
        getBytesHandle = linker.downcallHandle(getBytesSymbol, getBytesDesc)
    }

    override fun render(params: RenderParams): Int {
        Arena.ofConfined().use { arena ->
            val pNx = arena.allocate(ValueLayout.JAVA_INT, params.x.size)
            val pX = if (params.x.isNotEmpty()) {
                arena.allocateArray(ValueLayout.JAVA_DOUBLE, *params.x)
            } else {
                arena.allocate(ValueLayout.JAVA_DOUBLE, 0.0)
            }

            val pNy = arena.allocate(ValueLayout.JAVA_INT, params.y.size)
            val pY = if (params.y.isNotEmpty()) {
                arena.allocateArray(ValueLayout.JAVA_DOUBLE, *params.y)
            } else {
                arena.allocate(ValueLayout.JAVA_DOUBLE, 0.0)
            }

            val pNz = arena.allocate(ValueLayout.JAVA_INT, params.z.size)
            val pZ = if (params.z.isNotEmpty()) {
                arena.allocateArray(ValueLayout.JAVA_DOUBLE, *params.z)
            } else {
                arena.allocate(ValueLayout.JAVA_DOUBLE, 0.0)
            }

            val pNt = arena.allocate(ValueLayout.JAVA_INT, params.texts.size)
            val count = maxOf(1, params.texts.size)
            val pTexts = arena.allocateArray(ValueLayout.ADDRESS, count.toLong())
            for (i in params.texts.indices) {
                val bytes = params.texts[i].toByteArray(StandardCharsets.UTF_8)
                val strSeg = arena.allocate(bytes.size + 1L)
                strSeg.copyFrom(MemorySegment.ofArray(bytes))
                strSeg.set(ValueLayout.JAVA_BYTE, bytes.size.toLong(), 0.toByte())
                pTexts.setAtIndex(ValueLayout.ADDRESS, i.toLong(), strSeg)
            }

            val pOutputType = allocPPAnsiChar(arena, params.outputType)
            val pWidth = arena.allocate(ValueLayout.JAVA_INT, params.width)
            val pHeight = arena.allocate(ValueLayout.JAVA_INT, params.height)
            val pStyle = allocPPAnsiChar(arena, params.style)
            val pTitle = allocPPAnsiChar(arena, params.title)
            val pFooter = allocPPAnsiChar(arena, params.footer)

            val pHoriz = arena.allocate(ValueLayout.JAVA_INT, if (params.horiz) 1 else 0)
            val pCmd = arena.allocate(ValueLayout.JAVA_INT, if (params.commander) 1 else 0)
            val pGl = arena.allocate(ValueLayout.JAVA_INT, if (params.opengl) 1 else 0)
            val pBufSize = arena.allocate(ValueLayout.JAVA_INT, params.bufSize)

            val rc = renderHandle.invokeExact(
                pNx, pX,
                pNy, pY,
                pNz, pZ,
                pNt, pTexts,
                pOutputType,
                pWidth, pHeight,
                pStyle, pTitle, pFooter,
                pHoriz, pCmd, pGl,
                pBufSize
            ) as Int

            params.bufSize = pBufSize.get(ValueLayout.JAVA_INT, 0)
            return rc
        }
    }

    override fun getBytes(size: Int): ByteArray {
        if (size <= 0) return ByteArray(0)
        Arena.ofConfined().use { arena ->
            val imgBuf = arena.allocate(size.toLong())
            val pSize = arena.allocate(ValueLayout.JAVA_INT, size)
            getBytesHandle.invokeExact(imgBuf, pSize)
            val actual = pSize.get(ValueLayout.JAVA_INT, 0)
            val result = ByteArray(actual)
            MemorySegment.copy(imgBuf, 0, MemorySegment.ofArray(result), 0, actual.toLong())
            return result
        }
    }

    private fun allocPPAnsiChar(arena: Arena, s: String): MemorySegment {
        val bytes = s.toByteArray(StandardCharsets.UTF_8)
        val strSeg = arena.allocate(bytes.size + 1L)
        strSeg.copyFrom(MemorySegment.ofArray(bytes))
        strSeg.set(ValueLayout.JAVA_BYTE, bytes.size.toLong(), 0.toByte())
        val ptrSeg = arena.allocate(ValueLayout.ADDRESS)
        ptrSeg.set(ValueLayout.ADDRESS, 0, strSeg)
        return ptrSeg
    }
}

