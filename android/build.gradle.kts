buildscript {
    repositories {
        google()
        mavenCentral()
    }
    dependencies {
        classpath("com.android.tools.build:gradle:8.7.3")
    }
}

allprojects {
    repositories {
        google()
        mavenCentral()
        maven {
            url = java.net.URI("https://jitpack.io")
        }
    }
}

subprojects {
    project.evaluationDependsOn(":app")
}

subprojects {
    project.plugins.withId("com.android.library") {
        project.extensions.configure<com.android.build.gradle.BaseExtension>("android") {
            compileSdkVersion(35)
            buildToolsVersion("35.0.0")
        }
    }
    project.afterEvaluate {
        val android = project.extensions.findByName("android") as? com.android.build.gradle.BaseExtension
        android?.apply {
            compileSdkVersion(35)
            buildToolsVersion("35.0.0")
        }
    }
}

tasks.register<Delete>("clean") {
    delete(rootProject.layout.buildDirectory)
}
