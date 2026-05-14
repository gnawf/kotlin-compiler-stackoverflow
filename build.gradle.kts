plugins {
    kotlin("jvm") version "2.3.21"
    application
}

group = "com.example"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    maven {
        url = uri("https://s01.oss.sonatype.org/content/repositories/snapshots/")
    }
    maven {
        url = uri("https://oss.sonatype.org/content/repositories/snapshots/")
    }
}

dependencies {
    implementation("com.graphql-java:graphql-java:0.0.0-2026-02-11T23-30-56-2f0dc5b")
}

application {
    mainClass.set("com.example.MainKt")
}

kotlin {
    jvmToolchain(21)
}
