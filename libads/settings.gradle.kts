pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "libads"

// Module core: chứa interface, ad manager, cache, lifecycle...
include(":libads-core")

// Khi cần thêm network, include thêm module adapter tại đây, ví dụ:
// include(":libads-admob")
// include(":libads-facebook")

// (tuỳ chọn) app mẫu để test lib trong lúc dev
// include(":sample-app")
