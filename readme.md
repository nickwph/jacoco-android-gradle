# Android Jacoco Plugin

## What is it

A jacoco plugin that provides better options and support to your Android project. It pre-set exclusion list that suits most Android projects.

## Tasks

Tasks are generated once this plugin is applied. 

### Create JaCoCo HTML and XML Reports

| <code>jacoco{BuildType}</code> |
| ------------------------------ |
| <code>jacocoDebug</code>       | 
| <code>jacocoRelease</code>     | 
      
### Open JaCoCo HTML Report in browser (MacOSX)
These tasks are created only when <code>createHtmlReports</code> is true.

| <code>openJacocoHtml{BuildType}</code> |
| -------------------------------------- |
| <code>openJacocoHtmlDebug</code>       | 
| <code>openJacocoHtmlRelease</code>     | 

## Example

```groovy
buildscript {
    repositories {
        maven { url "https://oss.sonatype.org/service/local/staging/deploy/maven2" }
    }
    dependencies {
        classpath 'com.nicholasworkshop:gradle-android-jacoco:1.0.0'
    }
}

apply plugin: 'com.nicholasworkshop.jacoco'

// all preferences are optional
jacocoOptions {
    version '0.7.1.201405082137'
    outputDestination "${buildDir}/reports/jacoco"
    createHtmlReports true
    createBadges true                  // generate png bagde files
    exclude ['**/R.*', '**/R$*.*']     // define your own exclusion list
    deleteExcludedClassFiles true      // if your ci does not support exclusion list
}
```

