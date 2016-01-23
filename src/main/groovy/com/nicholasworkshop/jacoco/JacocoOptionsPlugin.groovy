package com.nicholasworkshop.jacoco

import org.gradle.api.GradleException
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.Exec
import org.gradle.testing.jacoco.tasks.JacocoReport

/**
 * Created by nickwph on 1/22/16.
 */
class JacocoOptionsPlugin implements Plugin<Project> {

    @Override
    void apply(Project project) {
        project.jacoco.setToolVersion(project.jacocoOptions.version)
        // set the default output destination
        if (project.jacocoOptions.outputDestination == null) {
            project.jacocoOptions.outputDestination = "${buildDir}/reports/jacoco"
        }
        // create jacoco tasks for each build variants
        def task = this.createMainJacocoTask()
        this.getBuildVariants().each { variant ->
            def variantTask = this.createJacocoVariantTasks(variant)
            task.dependsOn(variantTask)
            if (project.jacocoOptions.createHtmlReports) {
                this.createJacocoHtmlVariantTasks(variant)
            }
        }
    }

    def getBuildVariants() {
        if (project.plugins.hasPlugin('com.android.application')) {
            return android.applicationVariants;
        } else if (project.plugins.hasPlugin('com.android.library')) {
            return android.libraryVariants
        }
        throw new GradleException('Android plugin required')
    }

    def createMainJacocoTask() {
        return tasks.create(name: "jacoco") {
            group = "Reporting"
            description = "Generate Jacoco coverage reports"
        }
    }

    def createJacocoHtmlVariantTasks(variant) {
        def variantName = variant.getName()
        def variantNameCapitalized = variantName.capitalize()
        def outputDestination = "${project.jacocoOptions.outputDestination}/${variantName}"
        return tasks.create(name: "openJacocoHtml${variantNameCapitalized}", type: Exec, dependsOn: "jacoco${variantNameCapitalized}") {
            group = "Reporting"
            executable 'open'
            args "${outputDestination}/index.html"
        }
    }

    def createJacocoVariantTasks(variant) {
        def variantName = variant.getName()
        def variantNameCapitalized = variantName.capitalize()
        def buildTypeName = variant.buildType.getName()
        def outputDestination = "${project.jacocoOptions.outputDestination}/${variantName}"
        def xmlOutputPath = "${outputDestination}/index.xml"
        def dependentTask = "test${variantNameCapitalized}UnitTest"
        def classPath = "${buildDir}/intermediates/classes/${buildTypeName}"
        return tasks.create(name: "jacoco${variantNameCapitalized}", type: JacocoReport, dependsOn: [dependentTask]) {
            group = "Reporting"
            description = "Generate Jacoco coverage reports"
            classDirectories = fileTree(dir: classPath, excludes: project.jacocoOptions.excludes)
            additionalSourceDirs = files([android.sourceSets.main.java.srcDirs])
            sourceDirectories = files([android.sourceSets.main.java.srcDirs])
            executionData = files("${buildDir}/jacoco/${dependentTask}.exec")
            reports {
                xml.enabled = true
                xml.destination = xmlOutputPath
                html.enabled = project.jacocoOptions.createHtmlReports
                html.destination = outputDestination
            }
            doLast {
                // generate badge files
                if (project.jacocoOptions.createBadges) {
                    this.generateBadgeFromXmlReport(xmlOutputPath, outputDestination)
                }
                // delete class files, it is required if you use jacoco on screwdriver
                // because screwdriver does not have the option to exclude files
                if (project.jacocoOptions.deleteExcludedClassFiles) {
                    this.deleteExcludedClassFiles(classPath)
                }
            }
        }
    }

    def deleteExcludedClassFiles(classPath) {
        println("deleting intermiediate class files...")
        delete fileTree(dir: classPath, includes: project.jacocoOptions.excludes)
        delete fileTree(dir: "${buildDir}/intermediates/classes/test")
    }

    def generateBadgeFromXmlReport(String path, String outputDestination) {
        println("generating coverage badges...")
        XmlParser parser = new XmlParser()
        parser.setFeature("http://apache.org/xml/features/disallow-doctype-decl", false)
        parser.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false)
        def report = parser.parse(path)
        report.counter.each { counter ->
            String name = counter.'@type'.toLowerCase()
            int missed = Integer.parseInt((String) counter.'@missed')
            int covered = Integer.parseInt((String) counter.'@covered')
            float percentage = ((float) covered) / (covered + missed)
            this.generateBadge(name, percentage, outputDestination)
        }
    }

    def generateBadge(String name, float percentage, String outputDestination) {
        def colors = ['red', 'orange', 'yellow', 'yellowgreen', 'green', 'brightgreen']
        def percentageString = String.format("%.1f%%25", percentage * 100)
        def colorString = colors[(int) (percentage * 6)]
        def url = "http://b.repl.ca/v1/${name}-${percentageString}-${colorString}.png"
        def file = new File("${outputDestination}/badge-${name}.png").newOutputStream()
        printf("- %-11s: %.1f%% %s\n", name, percentage * 100, url)
        file << new URL(url).openStream()
        file.close()
    }
}
