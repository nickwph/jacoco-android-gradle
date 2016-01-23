package com.nicholasworkshop.android

import org.gradle.api.GradleException
import org.gradle.api.internal.project.DefaultProject
import org.gradle.testfixtures.ProjectBuilder
import org.testng.annotations.Test

/**
 * Created by nickwph on 1/23/16.
 */
class JacocoPluginTest {

    @Test
    void testApply() throws Exception {
        DefaultProject project = ProjectBuilder.builder().build() as DefaultProject
        project.apply(plugin: 'com.android.application')
        project.apply(plugin: 'com.nicholasworkshop.android.jacoco')
        project.android.compileSdkVersion 23
        project.android.buildToolsVersion "23.0.1"
        project.evaluate()
    }

    @Test(expectedExceptions = GradleException)
    void testApply_ifNoAndroidPlugin_thenThrowException() throws Exception {
        DefaultProject project = ProjectBuilder.builder().build() as DefaultProject
        project.apply plugin: 'com.nicholasworkshop.android.jacoco'
        project.evaluate()
    }
}
