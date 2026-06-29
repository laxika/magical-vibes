plugins {
    `java-test-fixtures`
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter")
    implementation("org.springframework.boot:spring-boot-starter-jackson")

    api(project(":magical-vibes-card"))
    api(project(":magical-vibes-networking"))
    api(project(":magical-vibes-scryfall"))

    // Shared card/AI test harness, consumed by backend and ai test suites.
    testFixturesApi(project(":magical-vibes-websocket"))
    testFixturesImplementation("org.springframework.boot:spring-boot-starter")
    testFixturesImplementation("org.springframework.boot:spring-boot-starter-jackson")
    testFixturesImplementation("org.junit.jupiter:junit-jupiter")
    testFixturesImplementation("org.assertj:assertj-core")
}
