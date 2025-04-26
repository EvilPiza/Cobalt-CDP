plugins {
    idea
    java
    id("com.github.johnrengelman.shadow") version "8.1.1"
    kotlin("jvm") version "1.9.22"
    `java-library`
    `maven-publish`
}

val baseGroup: String by project
val version: String by project
val modid: String by project

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(8))
    withSourcesJar()
    withJavadocJar()
}

sourceSets.main {
    output.setResourcesDir(sourceSets.main.flatMap { it.java.classesDirectory })
}

repositories {
    mavenCentral()
    maven("https://jitpack.io")
}

val shadowImpl: Configuration by configurations.creating {
    configurations.implementation.get().extendsFrom(this)
}

dependencies {
    implementation("com.github.MinnDevelopment:Java-DiscordRPC:v2.0.2") {
        exclude(group = "club.minnced", module = "discord-rpc-release")
    }
    shadowImpl("com.github.MinnDevelopment:Java-DiscordRPC:v2.0.2") {
        exclude(group = "club.minnced", module = "discord-rpc-release")
    }
    implementation(kotlin("stdlib-jdk8"))
}

tasks.withType<JavaCompile>().configureEach {
    options.isFork = true
    options.encoding = "UTF-8"
}

tasks.withType<Jar> {
    archiveBaseName.set(modid)
}

tasks.shadowJar {
    archiveClassifier.set("shaded")
    configurations = listOf(shadowImpl)
    relocate("club.minnced.discord.rpc", "$baseGroup.deps.discord.rpc")
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            artifact(tasks["jar"]) {
                classifier = ""
            }

            artifact(tasks["sourcesJar"]) {
                classifier = "sources"
            }

            artifact(tasks["javadocJar"]) {
                classifier = "javadoc"
            }

            groupId = "com.github.evilpiza"
            artifactId = modid
            version = project.version.toString()

            pom {
                name.set(modid)
                description.set("Your library description")
                url.set("https://github.com/evilpiza/cobalt-cdp")
                developers {
                    developer {
                        id.set("evilpiza")
                    }
                }
            }
        }
    }
}