package com.nicholasworkshop.android

import org.gradle.api.GradleException
import org.gradle.api.Project
import org.gradle.api.internal.project.DefaultProject
import org.gradle.testfixtures.ProjectBuilder
import org.junit.Test

/**
 * Created by nickwph on 1/23/16.
 */
class JacocoPluginTest {

    @Test
    void testApply() throws Exception {
        DefaultProject project = createAndroidApplicaitonProject()
        project.apply(plugin: 'com.nicholasworkshop.android.jacoco')
        project.evaluate()
    }

    @Test
    void testApply_whenAndroidLibraryIsUsed() throws Exception {
        DefaultProject project = createAndroidLibraryProject()
        project.apply(plugin: 'com.nicholasworkshop.android.jacoco')
        project.evaluate()
    }

    @Test(expected = GradleException)
    void testApply_ifNoAndroidPlugin_thenThrowException() throws Exception {
        DefaultProject project = ProjectBuilder.builder().build() as DefaultProject
        project.apply(plugin: 'com.nicholasworkshop.android.jacoco')
        project.evaluate()
    }


    private static Project createAndroidApplicaitonProject() {
        DefaultProject project = ProjectBuilder.builder().build() as DefaultProject
        linkAndroidSdkDir(project)
        generateAndroidManifest(project)
        project.apply(plugin: 'com.android.application')
        project.android.compileSdkVersion 23
        project.android.buildToolsVersion "23.0.1"
        return project
    }

    private static Project createAndroidLibraryProject() {
        DefaultProject project = ProjectBuilder.builder().build() as DefaultProject
        linkAndroidSdkDir(project)
        generateAndroidManifest(project)
        project.apply(plugin: 'com.android.library')
        project.android.compileSdkVersion 23
        project.android.buildToolsVersion "23.0.1"
        return project
    }

    private static void generateAndroidManifest(Project project) {
        File path = new File(project.projectDir.toString(), "src/main")
        File file = new File(path.toString(), "AndroidManifest.xml")
        path.mkdirs()
        file.createNewFile()
        file << "<manifest/>"
    }

    private static void linkAndroidSdkDir(DefaultProject project) {
        File file = new File(project.projectDir.toString(), "local.properties")
        File local = new File("local.properties")
        if (local.exists()) {
            file << local.text
        } else {
            FileOutputStream stream = new FileOutputStream(file)
            Properties properties = new Properties()
            properties.setProperty("sdk.dir", System.getenv("ANDROID_HOME"))
            properties.store(stream, null)
            stream.close()
        }
    }
}
