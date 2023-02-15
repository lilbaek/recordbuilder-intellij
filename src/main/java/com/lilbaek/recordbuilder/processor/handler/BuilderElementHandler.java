package com.lilbaek.recordbuilder.processor.handler;

import com.intellij.psi.PsiField;
import com.intellij.psi.PsiMethod;
import com.lilbaek.recordbuilder.util.Utils;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

public interface BuilderElementHandler {

    default String renderBuildPrepare(@NotNull BuilderInfo info) {
        return "";
    }

    Collection<PsiField> renderBuilderFields(@NotNull BuilderInfo info);

    default String calcBuilderMethodName(@NotNull BuilderInfo info) {
        return Utils.buildAccessorName(info.getSetterPrefix(), info.getFieldName());
    }

    Collection<PsiMethod> renderBuilderMethod(@NotNull BuilderInfo info);
}
