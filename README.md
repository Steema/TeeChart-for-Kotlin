# TeeChart for Kotlin

[TeeChart](https://steema.com/) charting / plotting / graphing library for [Kotlin](https://kotlinlang.org/) language, powered by the native
`teechart.dll` (Windows) or `libteechart.so` / `libteechart.dylib`
(Linux / macOS).

---

<img width="866" height="464" alt="image" src="https://github.com/user-attachments/assets/d13f9208-6822-4968-9fe2-d0c1b53cfc6a" />


### Notes

Tested on Windows with Kotlin 2.0+ and OpenJDK 21+.

The 64-bit DLL (`libs/win64/teechart.dll`) and Linux 64-bit shared library (`libs/linux64/libteechart.so`) are included in the repository.
On macOS you must supply the shared library separately and point
`TEECHART_LIB` to its location.

### Running in [Jetbrain](https://www.jetbrains.com/)'s [IntelliJ IDEA](https://www.jetbrains.com/idea/) ide:

<img width="1043" height="747" alt="image" src="https://github.com/user-attachments/assets/b16a1593-bb1e-4311-a89c-895cdc3282a3" />

---

### Quick start

Using Gradle:

```bash
./gradlew :examples:simple:run
```

This creates a pie chart in a modal window:

```kotlin
package com.steema.teechart.examples.simple

import com.steema.teechart.TeeChart

fun main() {
    TeeChart.new()
        .y(2.5, 3.2, 2.8, 4.1)
        .texts("One", "Two", "Three", "Four")
        .style("pie")
        .title("TeeChart for Kotlin")
        .output("modal")
        .size(800, 500)
        .render()

    println("TeeChartRender correctly executed")
}
```

---

### Rendering to an image file

Use `output("png")` (or `"jpeg"`) together with `renderBytes()` to get the
chart as a byte array and write it to disk:

```kotlin
package com.steema.teechart.examples.bitmap

import com.steema.teechart.TeeChart
import java.io.File

fun main() {
    val img = TeeChart.new()
        .y(2.5, 3.2, 2.8, 4.1)
        .texts("One", "Two", "Three", "Four")
        .style("pie")
        .title("TeeChart for Kotlin")
        .size(800, 500)
        .output("png")
        .renderBytes()

    val outFile = File("chart.png")
    outFile.writeBytes(img)
    println("Chart saved to ${outFile.name} (${img.size} bytes)")
}
```

Run the bitmap example:

```bash
./gradlew :examples:bitmap:run
```

---

### Library search order

The package locates the native library automatically:

1. `TEECHART_LIB` environment variable (file or folder)
2. `teechart.lib` JVM system property (`-Dteechart.lib=...`)
3. Next to the executable / running JAR (and in `libs/<platform>/` below it)
4. `libs/<platform>/` relative to the current working directory
5. `libs/<platform>/` relative to the project root

Set `TEECHART_LIB` to override all of the above:

```bash
set TEECHART_LIB=C:\path\to\teechart.dll
```

---

### Native Bridge & Compatibility

TeeChart for Kotlin automatically selects the optimal native bridge:
- **Java 21+ Foreign Function & Memory (FFM) API**: Zero external dependencies on modern JVMs.
- **JNA (Java Native Access)**: Universal compatibility when `net.java.dev.jna:jna` is on the classpath.

---

### Fluent API reference

| Method | Description |
|--------|-------------|
| `TeeChart.new()` / `teechart()` | Create a new chart with default settings |
| `.x(vararg v: Double)` / `.X(...)` | Set X values |
| `.y(vararg v: Double)` / `.Y(...)` | Set Y values |
| `.z(vararg v: Double)` / `.Z(...)` | Set Z values (3-D series) |
| `.texts(vararg v: String)` / `.Texts(...)` | Set point labels |
| `.style(s: String)` / `.Style(...)` | Series style: `"pie"`, `"bar"`, `"line"`, … |
| `.title(s: String)` / `.Title(...)` | Chart title |
| `.footer(s: String)` / `.Footer(...)` | Chart footer |
| `.size(w, h: Int)` / `.Size(...)` | Output size in pixels |
| `.output(s: String)` / `.Output(...)` | Output type: `"modal"`, `"window"`, `"png"`, `"jpeg"` |
| `.horizontal(b: Boolean)` / `.Horizontal(...)` | Draw series horizontally |
| `.commander(b: Boolean)` / `.Commander(...)` | Show the chart editor UI |
| `.openGL(b: Boolean)` / `.OpenGL(...)` | Enable OpenGL rendering |
| `.render()` / `.Render()` | Draw the chart (window or image file inside DLL) |
| `.renderBytes(): ByteArray` / `.RenderBytes()` | Render and return raw image bytes |
| `TeeChart.getBytes(size: Int)` | Retrieve bytes from the last Render call |
