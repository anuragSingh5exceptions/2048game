//plugins {
//    alias(libs.plugins.android.application)
//    alias(libs.plugins.jetbrains.kotlin.android)
//}
//
//android {
//    namespace = "com.example.myapplication"
//    compileSdk = 36
//
//    defaultConfig {
//        applicationId = "com.example.myapplication"
//        minSdk = 27
//        targetSdk = 36
//        versionCode = 1
//        versionName = "1.0"
//
//        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
//    }
//
//    buildTypes {
//        release {
//            isMinifyEnabled = false
//            proguardFiles(
//                getDefaultProguardFile("proguard-android-optimize.txt"),
//                "proguard-rules.pro"
//            )
//        }
//    }
//    compileOptions {
//        sourceCompatibility = JavaVersion.VERSION_17
//        targetCompatibility = JavaVersion.VERSION_17
//    }
//    kotlinOptions {
//        jvmTarget = "17"
//    }
//}
//
//dependencies {
//
//    implementation(libs.androidx.core.ktx)
//    implementation(libs.androidx.appcompat)
//    implementation(libs.material)
//    implementation(libs.androidx.activity)
//    implementation(libs.androidx.constraintlayout)
//    testImplementation(libs.junit)
//    androidTestImplementation(libs.androidx.junit)
//    androidTestImplementation(libs.androidx.espresso.core)
//}

import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.jetbrains.kotlin.android)
}

/**
 * Read version.properties
 */
val versionPropsFile = rootProject.file("version.properties")
val versionProps = Properties()

if (versionPropsFile.exists()) {
    versionProps.load(versionPropsFile.inputStream())
}

/**
 * dynamic version from CI
 */
val dynamicVersionCode =
    (project.findProperty("VERSION_CODE") as String?)?.toInt() ?: 1

val dynamicVersionName =
    project.findProperty("VERSION_NAME") as String? ?: "1.0.0"

/**
 * release notes text
 */
val releaseNotes = """
Environment: ${versionProps["Env"]}

Core SDK: ${versionProps["core"]}
Camera SDK: ${versionProps["camera"]}
Media SDK: ${versionProps["media"]}
Image SDK: ${versionProps["image"]}
Video SDK: ${versionProps["video"]}

App Version: $dynamicVersionName ($dynamicVersionCode)
""".trimIndent()

android {

    namespace = "com.example.myapplication"

    compileSdk = 36

    defaultConfig {

        applicationId = "com.example.myapplication"

        minSdk = 27

        targetSdk = 36

        versionCode = dynamicVersionCode

        versionName = dynamicVersionName

        testInstrumentationRunner =
            "androidx.test.runner.AndroidJUnitRunner"
    }
    signingConfigs {

        create("release") {

            storeFile = file(
                System.getenv("KEYSTORE_PATH") ?: "keystore.jks"
            )

            storePassword =
                System.getenv("KEYSTORE_PASSWORD")

            keyAlias =
                System.getenv("KEY_ALIAS")

            keyPassword =
                System.getenv("KEY_PASSWORD")
        }
    }

    buildTypes {

        release {

            isMinifyEnabled = false
            signingConfig = signingConfigs.getByName("release")

            proguardFiles(
                getDefaultProguardFile(
                    "proguard-android-optimize.txt"
                ),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {

        sourceCompatibility = JavaVersion.VERSION_17

        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {

        jvmTarget = "17"
    }
}

/**
 * create release notes file
 */
tasks.register("generateReleaseNotes") {

    doLast {

        val outputDir =
            layout.buildDirectory.get().asFile

        if (!outputDir.exists()) {
            outputDir.mkdirs()
        }

        val outputFile =
            File(outputDir, "release-notes.txt")

        outputFile.writeText(releaseNotes)

        println("Release notes generated")
    }
}


tasks.whenTaskAdded {

    if (name == "assembleRelease") {

        dependsOn("generateReleaseNotes")
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)

    implementation(libs.androidx.appcompat)

    implementation(libs.material)

    implementation(libs.androidx.activity)

    implementation(libs.androidx.constraintlayout)

    testImplementation(libs.junit)

    androidTestImplementation(libs.androidx.junit)

    androidTestImplementation(libs.androidx.espresso.core)
}