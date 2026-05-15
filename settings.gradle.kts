pluginManagement {
    repositories {
        google()   // 🔥 REQUIRED
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "NammaMela"
include(":app")