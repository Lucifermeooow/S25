plugins {
    id("com.android.application")
    id("kotlin-android")
    id("dev.flutter.flutter-gradle-plugin")
}

android {
    namespace = "com.ahmed.streamgit101"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.ahmed.streamgit101"
        minSdk = 24
        targetSdk = 35
        versionCode = 22
        versionName = "22.0.0"
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    signingConfigs {
        create("release") {
            val customKeystorePath = System.getenv("RELEASE_KEYSTORE_PATH")
            val customKeystoreFile = customKeystorePath?.let { file(it) }
            val fallbackKeystore = file("${rootDir}/release.keystore").takeIf { it.exists() }
                ?: file("${rootDir}/../debug.keystore").takeIf { it.exists() }
                ?: file("${rootDir}/debug.keystore")

            storeFile = if (customKeystoreFile != null && customKeystoreFile.exists()) customKeystoreFile else fallbackKeystore
            storePassword = System.getenv("RELEASE_KEYSTORE_PASSWORD") ?: "android"
            keyAlias = System.getenv("RELEASE_KEY_ALIAS") ?: "androiddebugkey"
            keyPassword = System.getenv("RELEASE_KEY_PASSWORD") ?: "android"
            enableV1Signing = true
            enableV2Signing = true
        }
    }

    buildTypes {
        release {
            signingConfig = signingConfigs.getByName("release")
            isMinifyEnabled = false
            isShrinkResources = false
        }
    }

    lint {
        abortOnError = false
        checkReleaseBuilds = false
    }
}

flutter {
    source = "../.."
}
