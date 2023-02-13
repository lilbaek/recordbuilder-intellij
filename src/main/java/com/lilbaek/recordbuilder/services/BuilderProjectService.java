package com.lilbaek.recordbuilder.services;

import com.intellij.openapi.project.Project;


public class BuilderProjectService {
    private final Project project;

    public BuilderProjectService(final Project project) {
        this.project = project;
        System.out.println("RecordBuilder starting");
    }
}
