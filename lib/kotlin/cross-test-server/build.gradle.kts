plugins {
    kotlin("jvm") version "1.5.31"
    id("com.ncorti.ktfmt.gradle") version "0.5.0"
    java
    application
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(platform("org.jetbrains.kotlin:kotlin-bom"))
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    // https://mvnrepository.com/artifact/org.jetbrains.kotlinx/kotlinx-coroutines-jdk8
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-jdk8:1.6.1")
    // https://mvnrepository.com/artifact/org.apache.thrift/libthrift
    implementation("org.apache.thrift:libthrift:INCLUDED")
    // https://mvnrepository.com/artifact/ch.qos.logback/logback-classic
    implementation("ch.qos.logback:logback-classic:1.3.0-alpha14")
    testImplementation("org.jetbrains.kotlin:kotlin-test")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit")
}

tasks {
    application {
        applicationName = "TestNonblockingServer"
        mainClass.set("org.apache.thrift.TestNonblockingServerKt")
    }

    ktfmt {
        kotlinLangStyle()
    }

    task<Exec>("compileThrift") {
        val thriftBin = if (hasProperty("thrift.compiler")) {
            file(property("thrift.compiler"))
        } else {
            project.rootDir.resolve("../../compiler/cpp/thrift")
        }
        val outputDir = layout.buildDirectory.dir("generated-sources")
        doFirst {
            mkdir(outputDir)
        }
        commandLine = listOf(
            thriftBin.absolutePath,
            "-gen",
            "kotlin",
            "-out",
            outputDir.get().toString(),
            project.rootDir.resolve("../../test/ThriftTest.thrift").absolutePath
        )
        group = LifecycleBasePlugin.BUILD_GROUP
    }

    compileKotlin {
        dependsOn("compileThrift")
    }
}

sourceSets["main"].java {
    srcDir(layout.buildDirectory.dir("generated-sources"))
}
