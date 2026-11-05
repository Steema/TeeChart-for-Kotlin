plugins {
    kotlin("jvm")
    application
}

application {
    mainClass.set("com.steema.teechart.examples.simple.MainKt")
}

dependencies {
    implementation(project(":teechart"))
}

