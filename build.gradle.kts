import org.jetbrains.gradle.plugins.docker.DockerImage

plugins {
    kotlin("jvm") version "1.9.22"
    kotlin("plugin.serialization") version "1.9.22"
    id("org.jetbrains.gradle.docker") version "1.6.0-RC.5"
    application
}

group = "net.senohrabek.jakub"
version = "0.0.2"

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.slf4j:slf4j-api:2.0.9")
    implementation("ch.qos.logback:logback-classic:1.5.2")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.0")
    implementation("aws.sdk.kotlin:ecs:1.0.78")
    implementation("io.ktor:ktor-client-apache5:2.3.9")
    testImplementation("org.jetbrains.kotlin:kotlin-test")
}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(17)
}

docker {
    fun DockerImage.setupJvmAppWithCustomArgs(runArgs: String, jvmArgs: String) {
        setupJvmApp("amazoncorretto", "17")
        buildArgs = mutableMapOf(
            "PARAMS" to runArgs,
            "JAVA_OPTS" to jvmArgs,
        )

        files {
            from("Dockerfile") {
                duplicatesStrategy = DuplicatesStrategy.INCLUDE
            }
        }
    }

    images {
        prometheusNodeFinder {
            setupJvmAppWithCustomArgs("", "")
        }
    }
}

application {
    mainClass.set("net.senohrabek.jakub.prometheus.nodefinder.MainKt")
}