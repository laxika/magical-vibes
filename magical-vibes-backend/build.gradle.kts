plugins {
    java
}

buildscript {
    repositories {
        mavenCentral()
    }
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

group = "com.magicalvibes"
version = "1.0.0-SNAPSHOT"
