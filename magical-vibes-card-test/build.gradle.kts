// Card tests split by starting letter, mirroring :magical-vibes-card, so they compile in parallel and
// editing a test only invalidates its own letter's test task. Every module depends on the whole card
// set: most card tests construct support cards of other letters (Grizzly Bears, basic lands, ...).
subprojects {
    dependencies {
        "testImplementation"(project(":magical-vibes-card"))
        "testImplementation"(project(":magical-vibes-engine"))
        "testImplementation"(testFixtures(project(":magical-vibes-engine")))
        "testImplementation"(project(":magical-vibes-ai"))
        "testImplementation"("org.springframework.boot:spring-boot-starter-jackson")
        "testImplementation"("org.springframework.boot:spring-boot-starter-test")
    }

    // Every test JVM pays Spring startup and a full JIT warm-up, so a few long-lived forks per letter
    // beat the root default of one fork per core: that started ~600 JVMs of ~400 tests each across
    // the 26 letters. Forks scale with the letter's size instead, ~300 test classes each. C1-only
    // JIT and two GC threads keep each fork near one busy core; the full run is CPU-bound, so that
    // buys more throughput than C2's faster steady state.
    val testClassCount = fileTree("src/test/java").matching { include("**/*Test.java") }.files.size
    tasks.withType<Test>().configureEach {
        maxParallelForks = ((testClassCount + 299) / 300).coerceIn(1, maxParallelForks)
        jvmArgs("-XX:TieredStopAtLevel=1", "-XX:ParallelGCThreads=2")
    }
}
