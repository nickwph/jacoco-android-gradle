package com.nicholasworkshop.android

import org.gradle.api.GradleException
import org.gradle.api.internal.project.DefaultProject
import org.gradle.testfixtures.ProjectBuilder
import org.junit.Test

/**
 * Created by nickwph on 1/23/16.
 */
class JacocoPluginTest {

    @Test
    void testApply() throws Exception {
        DefaultProject project = ProjectBuilder.builder().build() as DefaultProject
        linkAndroidSdkDir(project)
        project.apply(plugin: 'com.android.application')
        project.apply(plugin: 'com.nicholasworkshop.android.jacoco')
        project.android.compileSdkVersion 23
        project.android.buildToolsVersion "23.0.1"
        project.evaluate()
    }

    @Test(expected = GradleException)
    void testApply_ifNoAndroidPlugin_thenThrowException() throws Exception {
        DefaultProject project = ProjectBuilder.builder().build() as DefaultProject
        project.apply plugin: 'com.nicholasworkshop.android.jacoco'
        project.evaluate()
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
