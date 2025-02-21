import org.jetbrains.kotlin.load.kotlin.signatures

plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
    id("maven-publish")
    id("signing")
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
                groupId = "com.lucky.library"
                artifactId = "luckylibrary"
                version = "1.0.0"

                //包含aar
                from(components["release"])

                pom {
                    name.set("luckylibrary")
                    description.set("test my library")
                    url.set("https://github.com/erwoooo/mLirbary")
                    licenses {
                        license {
                            name.set("The Apache License, Version 2.0")
                            url.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
                        }
                    }
                    developers {
                        developer {
                            id.set("lucky")
                            name.set("erwoooo")
                            email.set("653429798@qq.com")
                        }
                    }

                    scm {
                        connection.set("scm:git:git://github.com/erwoooo/mLirbary.git")

                        developerConnection.set("scm:git:ssh://github.com/erwoooo/mLirbary.git")
                        url.set("https://github.com/erwoooo/mLirbary")
                    }
                }
            }
        }

        repositories{
            maven {
                name="GitHubPackages"
                url = uri("https://github.com/erwoooo/mLirbary")
                credentials {
                    username= (project.findProject("gpr.user")?:System.getenv("GPR_USER")) as? String
                    password = (project.findProject("gpr.key")?:System.getenv("GPR_KEY")) as? String
                }
            }
        }
    }

    signing{
        useInMemoryPgpKeys(System.getenv("SINGING_KEY"),System.getenv("SINGING_PASSWORD"))
        sign(publishing.publications["aar"])
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