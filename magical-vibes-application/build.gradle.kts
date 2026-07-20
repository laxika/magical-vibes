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

val copyFrontend = tasks.register<Copy>("copyFrontend") {
    dependsOn(":magical-vibes-frontend:buildAngular")
    from(project(":magical-vibes-frontend").file("dist/magical-vibes-frontend/browser"))
    into(layout.buildDirectory.dir("resources/main/static"))
}

tasks.named("processResources") {
    dependsOn(copyFrontend)
}
