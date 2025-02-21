import org.jetbrains.kotlin.load.kotlin.signatures

plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
    id("maven-publish")
//    id("signing")
}

android {
    namespace = "com.lucky.library"
    compileSdk = 33

    defaultConfig {
//        applicationId = "com.lucky.library"
        minSdk = 24
//        targetSdk = 33
//        versionCode = 1
//        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
}


afterEvaluate {
    publishing{
        publications{
            create<MavenPublication>("aar"){
                groupId = "com.github.erwoooo"
                artifactId = "mlirbary"
                version = "1.0.2"

                artifact("$buildDir/outputs/aar/app-release.aar")

            }
        }

        repositories{
            maven {
                name="GitHubPackages"
                url = uri("https://maven.pkg.github.com/erwoooo/mLirbary")
                credentials {
                    username= "erwoooo"
                    password = "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"
                }
            }
        }
    }

}

dependencies {

    implementation("androidx.core:core-ktx:1.9.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.8.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
}