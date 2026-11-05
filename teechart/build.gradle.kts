plugins {
    kotlin("jvm")
    `java-library`
}

dependencies {
    api("net.java.dev.jna:jna:5.14.0")
    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}

