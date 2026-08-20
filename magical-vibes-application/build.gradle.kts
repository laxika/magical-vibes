apply(plugin = "org.springframework.boot")

dependencies {
    implementation("org.springframework.boot:spring-boot-starter")
    implementation("org.springframework.boot:spring-boot-starter-jackson")
    implementation("org.springframework.boot:spring-boot-starter-websocket")

    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-liquibase")

    runtimeOnly("org.xerial:sqlite-jdbc:3.47.1.0")
    runtimeOnly("org.hibernate.orm:hibernate-community-dialects:6.6.4.Final")

    implementation(project(":magical-vibes-card"))
    implementation(project(":magical-vibes-engine"))
    implementation(project(":magical-vibes-ai"))
    implementation(project(":magical-vibes-card-data"))
    implementation(project(":magical-vibes-websocket"))
    implementation(project(":magical-vibes-webservice"))

    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.security:spring-security-crypto")
    testImplementation("io.github.classgraph:classgraph:4.8.179")
    testImplementation("com.tngtech.archunit:archunit-junit5:1.3.0")
    testImplementation(testFixtures(project(":magical-vibes-engine")))
}

group = "com.magicalvibes"
version = "1.0.0-SNAPSHOT"

// Sync, not Copy: Angular fingerprints its bundles, so every rebuild lands a new
// main-<hash>.js beside the old one and Copy leaves the old one there forever. That had
// accumulated 20 dead bundles and 3 dead stylesheets — 14 MB of unreachable files baked
// into the jar. Sync clears whatever is no longer in dist/, which is safe because this
// directory has no other contributor: the module ships no src/main/resources/static.
val copyFrontend = tasks.register<Sync>("copyFrontend") {
    dependsOn(":magical-vibes-frontend:buildAngular")
    from(project(":magical-vibes-frontend").file("dist/magical-vibes-frontend/browser"))
    into(layout.buildDirectory.dir("resources/main/static"))
    mustRunAfter("processResources")
}

tasks.named("bootJar") {
    dependsOn(copyFrontend)
}

tasks.named("bootRun") {
    dependsOn(copyFrontend)
}
