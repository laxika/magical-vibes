apply(plugin = "org.springframework.boot")

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-jackson")
    implementation("org.springframework.boot:spring-boot-starter-jdbc")
    implementation("org.springframework.boot:spring-boot-starter-liquibase")
    runtimeOnly("org.xerial:sqlite-jdbc:3.47.1.0")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
}

tasks.named<JavaExec>("bootRun") {
    workingDir = rootProject.projectDir
}
