dependencies {
    implementation("org.springframework.boot:spring-boot-starter")
    implementation("org.springframework.boot:spring-boot-starter-jackson")
    implementation("tools.jackson.dataformat:jackson-dataformat-cbor")

    api(project(":magical-vibes-card"))
}
