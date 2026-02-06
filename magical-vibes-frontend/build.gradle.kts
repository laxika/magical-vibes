plugins {
    base
    id("com.github.node-gradle.node") version "7.1.0"
}

group = "com.magicalvibes"
version = "1.0.0-SNAPSHOT"

node {
    // Whether to download and install a specific Node.js version or not
    download.set(true)
    // Version of Node.js to use
    version.set("24.13.0")
    // Version of npm to use
    npmVersion.set("11.6.2")
    // Base URL for fetching node distributions
    distBaseUrl.set("https://nodejs.org/dist")
    // Directory where Node.js is installed
    workDir.set(file("${project.projectDir}/.gradle/nodejs"))
    // Directory where npm packages are installed
    npmWorkDir.set(file("${project.projectDir}/.gradle/npm"))
    // Directory containing the package.json
    nodeProjectDir.set(file("${project.projectDir}"))
}

// Custom task to build the Angular application
tasks.register<com.github.gradle.node.npm.task.NpmTask>("buildAngular") {
    description = "Build the Angular application"
    dependsOn("npmInstall")
    args.set(listOf("run", "build"))
}

// Custom task to run Angular tests
tasks.register<com.github.gradle.node.npm.task.NpmTask>("testAngular") {
    description = "Run Angular tests"
    dependsOn("npmInstall")
    args.set(listOf("run", "test"))
}

// Make the build task depend on Angular build
tasks.named("build") {
    dependsOn("buildAngular")
}

// Clean task to remove node_modules and dist folders
tasks.register<Delete>("cleanNode") {
    description = "Clean node_modules and build output"
    delete("node_modules")
    delete("dist")
    delete(".gradle/nodejs")
    delete(".gradle/npm")
}

tasks.named("clean") {
    dependsOn("cleanNode")
}
