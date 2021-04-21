import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

repositories {
    mavenCentral()
}

plugins {
  java
  id("com.github.johnrengelman.shadow") version "6.1.0"
}

java {
  sourceCompatibility = JavaVersion.VERSION_1_8
  targetCompatibility = JavaVersion.VERSION_1_8
}

dependencies {
    implementation("org.apache.commons:commons-compress:1.20")
}

val shadowJar by tasks.getting(ShadowJar::class) {
  mergeServiceFiles()

  isZip64 = true
  archiveName = "tar-gz.jar"

  manifest {
    attributes(mapOf("Main-Class" to "targztest.Main"))
  }

}