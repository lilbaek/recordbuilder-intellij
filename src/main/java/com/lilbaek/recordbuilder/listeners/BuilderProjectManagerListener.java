package com.lilbaek.recordbuilder.listeners;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManagerListener;
import com.lilbaek.recordbuilder.services.BuilderProjectService;
import org.jetbrains.annotations.NotNull;

class BuilderProjectManagerListener implements ProjectManagerListener {

    @Override
    public void projectOpened(@NotNull final Project project) {
        BuilderProjectService service = project.getService(BuilderProjectService.class);
    }
}
