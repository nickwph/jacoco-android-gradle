package com.nicholasworkshop.android

import org.gradle.api.internal.project.DefaultProject
import org.gradle.testfixtures.ProjectBuilder
import org.testng.annotations.Test

/**
 * Created by nickwph on 1/23/16.
 */
class JacocoPluginTest {

    @Test
    void testApply() throws Exception {
        DefaultProject project = ProjectBuilder.builder().build();
        project.apply plugin: 'com.nicholasworkshop.android.jacoco'
        project.evaluate()
    }
}
