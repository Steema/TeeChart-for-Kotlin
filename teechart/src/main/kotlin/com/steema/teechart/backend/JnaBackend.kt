package com.steema.teechart.backend

import com.sun.jna.Library
import com.sun.jna.Memory
import com.sun.jna.Native
import com.sun.jna.Pointer
import com.sun.jna.ptr.IntByReference
import com.sun.jna.ptr.PointerByReference
import java.nio.charset.StandardCharsets

internal interface TeeChartJnaLib : Library {
    fun TeeChartRender(
        nx: IntByReference, x: Pointer?,
        ny: IntByReference, y: Pointer?,
        nz: IntByReference, z: Pointer?,
        ntexts: IntByReference, texts: Pointer?,
        outputType: PointerByReference?,
        width: IntByReference, height: IntByReference,
        style: PointerByReference?, title: PointerByReference?, footer: PointerByReference?,
        isHoriz: IntByReference,
        commander: IntByReference,
        opengl: IntByReference,
        bufSize: IntByReference
    ): Int

    fun TeeChartGetBytes(buf: Pointer, n: IntByReference)
}

/**
 * JNA-based implementation of [TeeChartBackend]. Works across all JVM versions
 * whenever net.java.dev.jna:jna is on the classpath.
 */
internal class JnaBackend(libPath: String) : TeeChartBackend {
    private val lib: TeeChartJnaLib = Native.load(libPath, TeeChartJnaLib::class.java)

    override fun render(params: RenderParams): Int {
        val nx = IntByReference(params.x.size)
        val xMem = toDoublePointer(params.x)

        val ny = IntByReference(params.y.size)
        val yMem = toDoublePointer(params.y)

        val nz = IntByReference(params.z.size)
        val zMem = toDoublePointer(params.z)

        val (textsMem, textMemories) = toTextPointers(params.texts)
        val ntexts = IntByReference(params.texts.size)

        val otRef = toPointerRef(params.outputType)
        val w = IntByReference(params.width)
        val h = IntByReference(params.height)
        val stRef = toPointerRef(params.style)
        val tiRef = toPointerRef(params.title)
        val foRef = toPointerRef(params.footer)

        val horiz = IntByReference(if (params.horiz) 1 else 0)
        val cmd = IntByReference(if (params.commander) 1 else 0)
        val gl = IntByReference(if (params.opengl) 1 else 0)
        val buf = IntByReference(params.bufSize)

        val rc = lib.TeeChartRender(
            nx, xMem,
            ny, yMem,
            nz, zMem,
            ntexts, textsMem,
            otRef,
            w, h,
            stRef, tiRef, foRef,
            horiz, cmd, gl,
            buf
        )

        params.bufSize = buf.value

        ReferenceHolder.keepAlive(textMemories, otRef, stRef, tiRef, foRef, xMem, yMem, zMem, textsMem)

        return rc
    }

    override fun getBytes(size: Int): ByteArray {
        if (size <= 0) return ByteArray(0)
        val buf = Memory(size.toLong())
        val sizeRef = IntByReference(size)
        lib.TeeChartGetBytes(buf, sizeRef)
        return buf.getByteArray(0, sizeRef.value)
    }

    private fun toDoublePointer(values: DoubleArray): Memory {
        val count = maxOf(1, values.size)
        val mem = Memory(count * 8L)
        if (values.isNotEmpty()) {
            mem.write(0, values, 0, values.size)
        } else {
            mem.setDouble(0, 0.0)
        }
        return mem
    }

    private fun toTextPointers(texts: List<String>): Pair<Memory, List<Memory>> {
        val ptrSize = Native.POINTER_SIZE.toLong()
        val textMemories = texts.map { text ->
            val bytes = text.toByteArray(StandardCharsets.UTF_8)
            val mem = Memory((bytes.size + 1).toLong())
            mem.write(0, bytes, 0, bytes.size)
            mem.setByte(bytes.size.toLong(), 0)
            mem
        }
        val count = maxOf(1, texts.size)
        val arrayMem = Memory(count * ptrSize)
        for (i in texts.indices) {
            arrayMem.setPointer(i * ptrSize, textMemories[i])
        }
        return Pair(arrayMem, textMemories)
    }

    private fun toPointerRef(str: String): PointerByReference {
        val bytes = str.toByteArray(StandardCharsets.UTF_8)
        val strMem = Memory((bytes.size + 1).toLong())
        strMem.write(0, bytes, 0, bytes.size)
        strMem.setByte(bytes.size.toLong(), 0)
        return PointerByReference(strMem)
    }

    private object ReferenceHolder {
        fun keepAlive(vararg obj: Any?) {}
    }
}

