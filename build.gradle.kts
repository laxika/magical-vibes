import io.spring.gradle.dependencymanagement.dsl.DependencyManagementExtension

plugins {
    id("org.springframework.boot") version "4.0.1" apply false
    id("io.spring.dependency-management") version "1.1.7" apply false
    id("com.diffplug.spotless") version "7.0.2" apply false
}

group = "com.magicalvibes"
version = "1.0.0"

subprojects {
    if (name != "magical-vibes-frontend") {
        apply(plugin = "java-library")
        apply(plugin = "io.spring.dependency-management")
        apply(plugin = "com.diffplug.spotless")

        repositories {
            mavenCentral()
        }

        configure<JavaPluginExtension> {
            toolchain {
                languageVersion.set(JavaLanguageVersion.of(21))
            }
        }

        configure<DependencyManagementExtension> {
            imports {
                mavenBom(org.springframework.boot.gradle.plugin.SpringBootPlugin.BOM_COORDINATES)
            }
        }

        dependencies {
            "compileOnly"("org.projectlombok:lombok")
            "annotationProcessor"("org.projectlombok:lombok")
            "testImplementation"("org.junit.jupiter:junit-jupiter")
            "testImplementation"("org.assertj:assertj-core")
            "testRuntimeOnly"("org.junit.platform:junit-platform-launcher")
        }

        configure<com.diffplug.gradle.spotless.SpotlessExtension> {
            // Opt-in only: run via the spotlessCheck/spotlessApply tasks; check/build don't trigger it
            isEnforceCheck = false
            java {
                target("src/**/*.java")
                removeUnusedImports()
            }
        }

        tasks.withType<Test> {
            useJUnitPlatform {
                if (System.getenv("CI") != null) {
                    // CI loads oracle data exclusively from MTGJSON; skip tests hitting the Scryfall API
                    excludeTags("scryfall-api")
                }
            }
            maxParallelForks = (Runtime.getRuntime().availableProcessors() * 3 / 4).coerceAtLeast(1)
            jvmArgs("-Xmx2g", "-XX:TieredStopAtLevel=1", "-XX:+UseParallelGC")
            forkEvery = 2000
            // Forward select system properties to the forked test JVM
            listOf("runCardFuzz", "runAiStress", "fuzzGames",
                    "runScenarioFuzz", "scenarioCard", "scenarioIterations", "scenarioSeed",
                    "layerBench", "mctsBench", "disableLayerBoardCache", "oracle.data-provider").forEach { prop ->
                System.getProperty(prop)?.let { systemProperty(prop, it) }
            }
            testLogging {
                // Benchmarks report through stdout — surface it on the console
                if (System.getProperty("layerBench") != null || System.getProperty("mctsBench") != null) {
                    showStandardStreams = true
                }
                events("failed")
                showExceptions = true
                showCauses = true
                showStackTraces = true
                exceptionFormat = org.gradle.api.tasks.testing.logging.TestExceptionFormat.SHORT
            }
        }

        tasks.withType<JavaCompile> {
            options.forkOptions.memoryMaximumSize =
                providers.gradleProperty("javaCompile.maxHeapSize").get()
        }
    }
}
