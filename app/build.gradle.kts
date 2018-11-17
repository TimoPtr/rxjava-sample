import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    java
    kotlin("jvm")
}


dependencies {
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8:1.3.10")

    implementation("io.reactivex.rxjava2:rxjava:2.2.3")
    implementation("nl.littlerobots.rxlint:rxlint:1.7.0")
    implementation("org.eclipse.paho:org.eclipse.paho.client.mqttv3:1.2.0")

    testImplementation("junit:junit:4.12")
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}