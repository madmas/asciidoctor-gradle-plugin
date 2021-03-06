/*
 * Copyright 2013-2019 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.asciidoctor.gradle.internal

import groovy.transform.CompileStatic
import org.asciidoctor.gradle.remote.AsciidoctorJavaExec
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.file.FileCollection
import org.gradle.util.GradleVersion
import org.ysb33r.grolifant.api.FileUtils

import static org.asciidoctor.gradle.base.AsciidoctorUtils.getClassLocation

/** Utilities for dealing with Asciidoctor in an external JavaExec process.
 *
 * @since 2.0* @author Sdhalk W. Cronjé
 */
@CompileStatic
class JavaExecUtils {

    /** The name of the Guava JAR used internally by Gradle.
     *
     */
    public static final String INTERNAL_GUAVA_NAME = internalGuavaName()

    /** Get the classpath that needs to be passed to the external Java process.
     *
     * @param project Current Gradle project
     * @param asciidoctorClasspath External asciidoctor dependencies
     * @param addInternalGuava Set to {@code true} to add internal Guava to classpath
     * @return A computed classpath that can be given to an external Java process.
     */
    static FileCollection getJavaExecClasspath(
        final Project project,
        final FileCollection asciidoctorClasspath,
        boolean addInternalGuava = false
    ) {
        File entryPoint = getClassLocation(AsciidoctorJavaExec)
        File groovyJar = getClassLocation(GroovyObject)

        FileCollection fc = project.files(entryPoint, groovyJar, asciidoctorClasspath)

        addInternalGuava ? project.files(fc, new File(project.gradle.gradleHomeDir, "lib/${INTERNAL_GUAVA_NAME}")) : fc
    }

    /** The file to which execution configuration data can be serialised to.
     *
     * @param task Task for which execution data will be serialised.
     * @return File that will (eventually) contain the execution data.
     */
    static File getExecConfigurationDataFile(final Task task) {
        task.project.file("${task.project.buildDir}/tmp/${FileUtils.toSafeFileName(task.name)}.javaexec-data")
    }

    /** Serializes execution configuration data.
     *
     * @param task Task for which execution data will be serialised.
     * @param executorConfigurations Executor configuration to be serialised
     * @return File that the execution data was written to.
     */

    static File writeExecConfigurationData(final Task task, Iterable<ExecutorConfiguration> executorConfigurations) {
        File execConfigurationData = getExecConfigurationDataFile(task)
        execConfigurationData.parentFile.mkdirs()
        ExecutorConfigurationContainer.toFile(execConfigurationData, executorConfigurations)
        execConfigurationData
    }

    /** Returns the location of the local Groovy Jar that is used by Gradle.
     *
     * @return Location on filesystem where the Groovy Jar is located.
     */
    static File getLocalGroovy() {
        getClassLocation(GroovyObject)
    }

    private static String internalGuavaName() {
        if (GradleVersion.current() >= GradleVersion.version('5.5')) {
            'guava-27.1-android.jar'
        } else if (GradleVersion.current() >= GradleVersion.version('5.0')) {
            'guava-26.0-android.jar'
        } else {
            'guava-jdk5-17.0.jar'
        }
    }
}
