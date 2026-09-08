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

tasks.register<Delete>("clean") {
    delete(rootProject.layout.buildDirectory)
}
