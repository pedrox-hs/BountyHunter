package dev.tigrao.bountyhunter.tracker

import dev.tigrao.bountyhunter.tracker.ext.dependentModules
import dev.tigrao.bountyhunter.tracker.ext.getTasksFromProjects
import groovy.lang.Closure
import org.gradle.api.DefaultTask
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.tasks.TaskAction
import org.gradle.language.base.plugins.LifecycleBasePlugin

open class TrackerTask : DefaultTask() {

    // region Task fields
    // region Configurable fields
    var defaultBranch: String = "master"

    var runTasks: Collection<String> = listOf()
    // endregion

    // region Lazy fields
    private val gitClient: GitClient by lazy {
        GitClientImpl(project.projectDir, logger, defaultBranch = defaultBranch)
    }

    private val affectedModules: AffectedModules by lazy {
        AffectedModules(project)
    }

    private val allAffectedFiles: List<String> by lazy {
        gitClient.findChangesFromPrincipalBranch()
    }

    private val tasksToRun: Collection<String> by lazy {
        project.getTasksFromProjects(getProjectsToRun(), runTasks)
    }
    // endregion
    // endregion

    init {
        description = "Run tasks with modules affected in branch"
        group = LifecycleBasePlugin.VERIFICATION_GROUP
    }

    @TaskAction
    fun sayHello() {
        val msg = if (tasksToRun.isNotEmpty()) "All tasks are executed successful"
        else "No tasks to execute"

        logger.lifecycle(msg)
    }

    override fun configure(closure: Closure<*>): Task =
        super.configure(closure).apply {
            dependsOn(*tasksToRun.toTypedArray())
        }

    private fun getProjectsToRun(): Collection<Project> {
        val (affectedModules, nonModuleFiles) =
            affectedModules.getAffectedModules(allAffectedFiles)

        val collection = affectedModules.toSortedSet()

        if (nonModuleFiles.isNotEmpty())
            return setOf(project.rootProject)

        collection += affectedModules.flatMap { module ->
            module.dependentModules
        }

        return collection
    }
}
