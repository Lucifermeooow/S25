pluginManagement {
    val flutterSdkPath =
        run {
            val properties = java.util.Properties()

            file("local.properties")
                .inputStream()
                .use { properties.load(it) }

            properties.getProperty("flutter.sdk")
                ?: error("flutter.sdk not set in local.properties")
        }

    includeBuild(
        "$flutterSdkPath/packages/flutter_tools/gradle"
    )

    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()

        maven {
            url = java.net.URI("https://jitpack.io")
        }
    }
}

plugins {
    id("dev.flutter.flutter-plugin-loader") version "1.0.0"

    id("com.android.application") version "9.0.1" apply false
}

dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()

        maven {
            url = java.net.URI("https://jitpack.io")
        }
    }
}

rootProject.name = "streamgit101"

include(":app")
