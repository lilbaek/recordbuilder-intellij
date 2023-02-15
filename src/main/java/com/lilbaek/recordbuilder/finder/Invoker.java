package com.lilbaek.recordbuilder.finder;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.psi.PsiAnnotation;
import com.intellij.psi.PsiClass;
import com.lilbaek.recordbuilder.processor.handler.BuilderHandler;
import com.lilbaek.recordbuilder.util.PsiAnnotationSearchUtil;

import java.util.Optional;

public class Invoker {
    public static Optional<PsiClass> getPsiClass(final PsiClass psiClass) {
        if (!psiClass.isRecord()) {
            return Optional.empty();
        }
        final PsiAnnotation psiAnnotation = PsiAnnotationSearchUtil.findAnnotation(psiClass, "com.lilbaek.recordbuilder.RecordBuilder");
        if (psiAnnotation != null) {
            final BuilderHandler builderHandler = ApplicationManager.getApplication().getService(BuilderHandler.class);
            return builderHandler.createBuilderClassIfNotExist(psiClass, null, psiAnnotation);
        }
        return Optional.empty();
    }

}
