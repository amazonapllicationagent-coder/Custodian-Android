plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
}

val signingStoreFile = providers.gradleProperty("CUSTODIAN_STORE_FILE").orNull
val signingStorePassword = providers.gradleProperty("CUSTODIAN_STORE_PASSWORD").orNull
val signingKeyAlias = providers.gradleProperty("CUSTODIAN_KEY_ALIAS").orNull
val signingKeyPassword = providers.gradleProperty("CUSTODIAN_KEY_PASSWORD").orNull
val signingConfigured = listOf(
    signingStoreFile,
    signingStorePassword,
    signingKeyAlias,
    signingKeyPassword
).all { !it.isNullOrBlank() }

android {
    namespace = "com.custodian.android"
    compileSdk = 35
    defaultConfig {
        applicationId = "com.custodian.android"
        minSdk = 24
        targetSdk = 35
        versionCode = 3
        versionName = "1.0.0"
    }

    if (signingConfigured) {
        signingConfigs {
            create("custodianRelease") {
                storeFile = file(signingStoreFile!!)
                storePassword = signingStorePassword
                keyAlias = signingKeyAlias
                keyPassword = signingKeyPassword
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            if (signingConfigured) {
                signingConfig = signingConfigs.getByName("custodianRelease")
            }
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions { jvmTarget = "17" }
}

dependencies {
    implementation("androidx.core:core-ktx:1.15.0")
    implementation("androidx.activity:activity-compose:1.10.1")
    implementation(platform("androidx.compose:compose-bom:2025.01.00"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    debugImplementation("androidx.compose.ui:ui-tooling")
}