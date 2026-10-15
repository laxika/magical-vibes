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
}
