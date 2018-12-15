import org.jetbrains.kotlin.gradle.plugin.KotlinSourceSet

plugins {
    kotlin("jvm") version "1.3.10"
}

val kotlinVersion = "1.3.10"

sourceSets {
    getByName("main") {
        withConvention(KotlinSourceSet::class) {
            kotlin.srcDir("src")
        }
    }
}

repositories {
    jcenter()
}

dependencies {
    compile(kotlin("stdlib", kotlinVersion))
    compile(kotlin("script-util", kotlinVersion))
}