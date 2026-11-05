plugins {
    kotlin("jvm")
    application
}

application {
    mainClass.set("com.steema.teechart.examples.bitmap.MainKt")
}

dependencies {
    implementation(project(":teechart"))
}

