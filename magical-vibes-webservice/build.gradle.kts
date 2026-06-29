dependencies {
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-jackson")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")

    implementation(project(":magical-vibes-card"))
    implementation(project(":magical-vibes-engine"))
    implementation(project(":magical-vibes-ai"))
    implementation(project(":magical-vibes-scryfall"))
    implementation(project(":magical-vibes-websocket"))
}
