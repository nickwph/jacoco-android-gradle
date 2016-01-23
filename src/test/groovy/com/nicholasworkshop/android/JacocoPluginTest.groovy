package com.nicholasworkshop.android

import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import org.testng.annotations.Test

/**
 * Created by nickwph on 1/23/16.
 */
class JacocoPluginTest {

    @Test
    void testApply() throws Exception {
        Project project = ProjectBuilder.builder().build();
        project.apply plugin: 'com.nicholasworkshop.android.jacoco'
    }
}
