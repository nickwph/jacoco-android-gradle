package com.nicholasworkshop.android

import org.gradle.api.GradleException
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.tasks.Exec
import org.gradle.testing.jacoco.tasks.JacocoReport

/**
 * Created by nickwph on 1/22/16.
 */
class JacocoPlugin implements Plugin<Project> {

    private Project project;

    @Override
    void apply(Project project) {
        this.project = project;

        project.apply plugin: 'jacoco'
        project.extensions.create('jacocoOptions', JacocoOptionsExtension)

        project.afterEvaluate {
            project.jacoco.setToolVersion(project.jacocoOptions.version)
            // set the default output destination
            if (project.jacocoOptions.outputDestination == null) {
                project.jacocoOptions.outputDestination = "${project.buildDir}/reports/jacoco"
            }
            // create jacoco tasks for each build variants
            Task task = this.createMainJacocoTask()
            getBuildVariants().each { variant ->
                def variantTask = this.createJacocoVariantTasks(project, variant)
                task.dependsOn(variantTask)
                if (project.jacocoOptions.createHtmlReports) {
                    this.createJacocoHtmlVariantTasks(project, variant)
                }
            }
        }
    }

    Task getBuildVariants() {
        if (project.plugins.hasPlugin('com.android.application')) {
            return project.android.applicationVariants;
        } else if (project.plugins.hasPlugin('com.android.library')) {
            return project.android.libraryVariants
        }
        throw new GradleException('Android plugin required')
    }

    Task createMainJacocoTask() {
        return project.tasks.create(name: "jacoco") {
            group = "Reporting"
            description = "Generate Jacoco coverage reports"
        }
    }

    Task createJacocoHtmlVariantTasks(variant) {
        String variantName = variant.getName()
        String variantNameCapitalized = variantName.capitalize()
        String outputDestination = "${project.jacocoOptions.outputDestination}/${variantName}"
        return project.tasks.create(name: "openJacocoHtml${variantNameCapitalized}", type: Exec, dependsOn: "jacoco${variantNameCapitalized}") {
            group = "Reporting"
            executable 'open'
            args "${outputDestination}/index.html"
        }
    }

    Task createJacocoVariantTasks(variant) {
        String variantName = variant.getName()
        String variantNameCapitalized = variantName.capitalize()
        String buildTypeName = variant.buildType.getName()
        String outputDestination = "${project.jacocoOptions.outputDestination}/${variantName}"
        String xmlOutputPath = "${outputDestination}/index.xml"
        String dependentTask = "test${variantNameCapitalized}UnitTest"
        String classPath = "${project.buildDir}/intermediates/classes/${buildTypeName}"
        return project.tasks.create(name: "jacoco${variantNameCapitalized}", type: JacocoReport, dependsOn: [dependentTask]) {
            group = "Reporting"
            description = "Generate Jacoco coverage reports"
            classDirectories = project.fileTree(dir: classPath, excludes: project.jacocoOptions.excludes)
            additionalSourceDirs = project.files([project.android.sourceSets.main.java.srcDirs])
            sourceDirectories = project.files([project.android.sourceSets.main.java.srcDirs])
            executionData = project.files("${project.buildDir}/jacoco/${dependentTask}.exec")
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
                    this.deleteExcludedClassFiles(project, classPath)
                }
            }
        }
    }

    def deleteExcludedClassFiles(classPath) {
        println("deleting intermiediate class files...")
        project.delete project.fileTree(dir: classPath, includes: project.jacocoOptions.excludes)
        project.delete project.fileTree(dir: "${project.buildDir}/intermediates/classes/test")
    }

    void generateBadgeFromXmlReport(String path, String outputDestination) {
        println("generating coverage badges...")
        XmlParser parser = new XmlParser()
        parser.setFeature("http://apache.org/xml/features/disallow-doctype-decl", false)
        parser.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false)
        Node report = parser.parse(path)
        report.counter.each { counter ->
            String name = counter.'@type'.toLowerCase()
            int missed = Integer.parseInt((String) counter.'@missed')
            int covered = Integer.parseInt((String) counter.'@covered')
            float percentage = ((float) covered) / (covered + missed)
            generateBadge(name, percentage, outputDestination)
        }
    }

    void generateBadge(String name, float percentage, String outputDestination) {
        String[] colors = ['red', 'orange', 'yellow', 'yellowgreen', 'green', 'brightgreen']
        String percentageString = String.format("%.1f%%25", percentage * 100)
        String colorString = colors[(int) (percentage * 6)]
        URL url = new URL("http://b.repl.ca/v1/${name}-${percentageString}-${colorString}.png")
        OutputStream file = new File("${outputDestination}/badge-${name}.png").newOutputStream()
        printf("- %-11s: %.1f%% %s\n", name, percentage * 100, url)
        file << url.openStream()
        file.close()
    }
}
