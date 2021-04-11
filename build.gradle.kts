import com.github.benmanes.gradle.versions.updates.DependencyUpdatesTask

plugins {
    kotlin("js") version "1.4.32"
    id("com.github.ben-manes.versions") version "0.38.0"
}

group = "org.jraf"
version = "1.0.0"

repositories {
    mavenCentral()
}

tasks {
    wrapper {
        distributionType = Wrapper.DistributionType.ALL
        gradleVersion = "7.0"
    }

    // Configuration for gradle-versions-plugin
    // Run `./gradlew dependencyUpdates` to see latest versions of dependencies
    withType<DependencyUpdatesTask> {
        resolutionStrategy {
            componentSelection {
                all {
                    if (
                        setOf("alpha", "beta", "rc", "preview", "eap", "m1", "m2").any {
                            candidate.version.contains(it, true)
                        }
                    ) {
                        reject("Non stable")
                    }
                }
            }
        }
    }
}

dependencies {
    implementation(kotlin("stdlib-js"))
//    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core-js:1.4.3")
}

kotlin {
    js(IR) {
        browser {
            binaries.executable()
        }
    }
}

// Run `./gradlew browserDevelopmentWebpack` during dev and `./gradlew browserProductionWebpack` to release
