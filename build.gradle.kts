plugins {
    kotlin("jvm") version "2.3.21"
    application
}

group = "com.example"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.jspecify:jspecify:1.0.0")
}

application {
    mainClass.set("com.example.MainKt")
}

kotlin {
    jvmToolchain(21)
}
