package com.nicholasworkshop.jacoco
/**
 * Created by nickwph on 1/22/16.
 */
class JacocoOptionsExtension {

    String version = '0.7.1.201405082137'

    String outputDestination

    boolean createHtmlReports = true

    boolean createBadges = true

    boolean deleteExcludedClassFiles = false

    Iterable<String> excludes = [
            // android
            '**/R.*',
            '**/R$*.*',
            '**/BuildConfig.*',
            // auto-value
            '**/AutoValue_*',
            // butter-knife
            '**/*$ViewBinder.*',
            '**/*$ViewBinder$*.*',
            // dagger
            '**/*_MembersInjector.*',
            '**/*_Factory.*',
            '**/*_Provide*Factory.*',
            '**/Dagger*Component.*',
            '**/Dagger*Component$Builder.*',
            // yahoo
            'com/yahoo/mobile/client/ApplicationConfig.*'
    ]
}