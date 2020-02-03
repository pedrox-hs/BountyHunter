plugins {
    kotlin("jvm")
    groovy
    maven
}

group = "dev.tigrao"
version = "0.0.2"

dependencies {
    implementation(kotlin("stdlib-jdk8"))
    implementation(gradleApi())
    implementation(localGroovy())
}
