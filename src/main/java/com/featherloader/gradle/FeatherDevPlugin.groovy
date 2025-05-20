// In buildSrc/src/main/groovy/com/featherloader/gradle/FeatherDevPlugin.groovy
package com.featherloader.gradle

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.JavaExec

class FeatherDevPlugin implements Plugin<Project> {
    void apply(Project project) {
        project.extensions.create('featherDev', FeatherDevExtension)

        project.afterEvaluate {
            project.tasks.register('runClient', JavaExec) {
                group = 'featherloader'
                description = 'Runs Minecraft client with FeatherLoader in development mode'

                mainClass.set('com.featherloader.dev.DevLauncher')
                classpath = project.sourceSets.main.runtimeClasspath

                jvmArgs = [
                "-Dfeatherloader.minecraft.dir=${project.featherDev.minecraftDir}",
                        "-Dfeatherloader.dev.mods=${project.projectDir}",
                        "-Dfeatherloader.mixins=${project.featherDev.enableMixins}",
                        "-Dfeatherloader.debug=true"
                ]

                // You'd need to generate appropriate Minecraft arguments here
                args = []

                dependsOn project.tasks.classes
            }

            // Add server task too if desired
        }
    }
}

class FeatherDevExtension {
    String minecraftDir = "${System.properties['user.home']}/.minecraft"
    boolean enableMixins = true
    String minecraftVersion = "1.21"
}