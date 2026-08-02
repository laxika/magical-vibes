import io.spring.gradle.dependencymanagement.dsl.DependencyManagementExtension
import net.ltgt.gradle.errorprone.CheckSeverity
import net.ltgt.gradle.errorprone.errorprone

plugins {
    id("org.springframework.boot") version "4.0.1" apply false
    id("io.spring.dependency-management") version "1.1.7" apply false
    id("com.diffplug.spotless") version "7.0.2" apply false
    id("net.ltgt.errorprone") version "5.1.0" apply false
}

group = "com.magicalvibes"
version = "1.0.0"

subprojects {
    if (name != "magical-vibes-frontend") {
        apply(plugin = "java-library")
        apply(plugin = "io.spring.dependency-management")
        apply(plugin = "com.diffplug.spotless")
        apply(plugin = "net.ltgt.errorprone")

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
            "errorprone"("com.google.errorprone:error_prone_core:2.42.0")
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

        // Opt-in unused-variable tooling. Normal compile/build keeps Error Prone disabled.
        // Requested task names are read at configuration time so only the targeted module(s)
        // enable UnusedVariable — dependency modules compile as usual.
        val requestedTasks = gradle.startParameter.taskNames
        val wantsUnusedVarScan = requestedTasks.any {
            it == "scanUnusedVariables" || it == "$path:scanUnusedVariables"
        }
        val wantsUnusedVarRemove = requestedTasks.any {
            it == "removeUnusedVariables" || it == "$path:removeUnusedVariables"
        }
        val unusedVarMode = when {
            wantsUnusedVarRemove -> "remove"
            wantsUnusedVarScan -> "scan"
            else -> null
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

        tasks.withType<JavaCompile>().configureEach {
            options.forkOptions.memoryMaximumSize = "2g"
            options.errorprone {
                // Keep EP off for ordinary builds; enable only for scan/remove on this project.
                enabled.set(unusedVarMode != null)
                disableWarningsInGeneratedCode.set(true)
                if (unusedVarMode != null) {
                    disableAllChecks.set(true)
                    check("UnusedVariable" to CheckSeverity.WARN)
                    if (unusedVarMode == "remove") {
                        errorproneArgs.add("-XepPatchChecks:UnusedVariable")
                        errorproneArgs.add("-XepPatchLocation:IN_PLACE")
                    }
                }
            }
        }

        tasks.register("scanUnusedVariables") {
            group = "verification"
            description =
                "Report unused locals / private fields / private-method params (Error Prone UnusedVariable). Does not modify sources."
            dependsOn(tasks.withType<JavaCompile>())
        }

        tasks.register("removeUnusedVariables") {
            group = "formatting"
            description =
                "Apply Error Prone UnusedVariable patches in-place for this module. Review git diff afterwards; do not commit until asked."
            dependsOn(tasks.withType<JavaCompile>())
            doLast {
                logger.lifecycle(
                    "[{}] UnusedVariable in-place patches applied (if any). Review the git diff before committing.",
                    name
                )
            }
        }
    }
}
