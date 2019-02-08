import com.jfrog.bintray.gradle.BintrayExtension.PackageConfig
import com.jfrog.bintray.gradle.BintrayExtension.VersionConfig
import de.mannodermaus.gradle.plugins.junit5.junitPlatform
import org.jetbrains.dokka.gradle.DokkaTask

plugins {
    id("org.jetbrains.dokka")
    id("digital.wup.android-maven-publish") version Versions.androidMavenPublishPlugin
    id("com.android.library")
    id("com.jfrog.bintray")
    id("de.mannodermaus.android-junit5")
    kotlin("android")
    `maven-publish`
}

apply(from = "../publishing.gradle.kts")
@Suppress("UNCHECKED_CAST")
val addCommonPomAttributes = extra["addCommonPomAttributes"] as (MavenPublication) -> Unit

android {
    compileSdkVersion(Android.compileSdkVersion)

    defaultConfig {
        minSdkVersion(Android.minSdkVersion)
        targetSdkVersion(Android.targetSdkVersion)
    }

    sourceSets {
        getByName("main").java.srcDirs("src/main/kotlin")
        getByName("test").java.srcDirs("src/test/kotlin")
    }

    buildTypes {
        getByName("release") {
            isMinifyEnabled = true
            proguardFiles(getDefaultProguardFile("proguard-android.txt"), "proguard-rules.pro")
            consumerProguardFiles("proguard-consumer-rules.pro")
        }
    }

    testOptions {
        junitPlatform {
            filters {
                includeEngines("spek2")
            }
        }
    }
}

dependencies {
    api(project(":core"))
    api(kotlin("stdlib"))
    api(Dependencies.androidXCollection)
    api(Dependencies.androidXFragment)

    testImplementation(Dependencies.kluent) {
        exclude(group = "org.jetbrains.kotlin")
    }
    testImplementation(Dependencies.spekApi) {
        exclude(group = "org.jetbrains.kotlin")
    }
    testRuntimeOnly(Dependencies.spek2RunnerJunit5) {
        exclude(group = "org.jetbrains.kotlin")
    }
    testRuntimeOnly(kotlin("stdlib-jdk8", version = Versions.kotlin))
}

val dokka = tasks.withType(DokkaTask::class) {
    outputFormat = "javadoc"
    outputDirectory = "$buildDir/dokkaJavadoc"
}

val sourcesJar by tasks.registering(Jar::class) {
    classifier = "sources"
    from(project.android.sourceSets["main"].java.srcDirs)
}

val javaDoc by tasks.registering(Jar::class) {
    dependsOn(dokka)
    classifier = "javadoc"
    from("$buildDir/dokkaJavadoc")
}

publishing {
    publications {
        create<MavenPublication>("katana-android") {
            from(components["android"])
            artifact(sourcesJar.get())
            artifact(javaDoc.get())
            artifactId = "katana-android"
            addCommonPomAttributes(this)
        }
    }
}

bintray {
    user = System.getenv("BINTRAY_USER")
    key = System.getenv("BINTRAY_API_KEY")
    override = false
    publish = true

    setPublications("katana-android")

    pkg(delegateClosureOf<PackageConfig> {
        repo = "katana"
        name = "katana-android"
        userOrg = "rewe-digital"
        websiteUrl = "https://github.com/rewe-digital/katana"
        vcsUrl = "https://github.com/rewe-digital/katana"
        setLicenses("MIT")

        version(delegateClosureOf<VersionConfig> {
            name = "${project.version}"
        })
    })
}
