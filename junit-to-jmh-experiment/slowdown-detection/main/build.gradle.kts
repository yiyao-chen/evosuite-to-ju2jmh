plugins {
    java
    id("me.champeau.jmh") version "0.6.6"
}

dependencies {
    val jUnit4Version: String by rootProject.extra
    val javaparserVersion: String by rootProject.extra

    implementation("junit", "junit", jUnit4Version)
    implementation("com.github.javaparser", "javaparser-core", javaparserVersion)


    implementation(files("../evosuite-1.2.0.jar"))

    implementation(files("../evosuite-standalone-runtime-1.2.0.jar"))


    testImplementation("junit", "junit", jUnit4Version)
}

tasks {
    test {
        outputs.upToDateWhen { false }
    }
}

configurations.register("testArchive") {
    extendsFrom(configurations.testImplementation.get())
}

tasks.register<Jar>(name = "jarTest") {
    from(project.sourceSets.test.get().output)
    archiveClassifier.set("test")
}

artifacts {
    add("testArchive", tasks.getByName("jarTest"))
}


