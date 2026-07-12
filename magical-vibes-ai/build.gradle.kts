dependencies {
    implementation("org.springframework.boot:spring-boot-starter")
    implementation("org.springframework.boot:spring-boot-starter-jackson")
    // Compiled against directly by SimulationLogSuppressor (TurboFilter)
    implementation("ch.qos.logback:logback-classic")

    implementation(project(":magical-vibes-engine"))
    implementation(project(":magical-vibes-websocket"))

    testImplementation(testFixtures(project(":magical-vibes-engine")))
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("io.github.classgraph:classgraph:4.8.179")
}
