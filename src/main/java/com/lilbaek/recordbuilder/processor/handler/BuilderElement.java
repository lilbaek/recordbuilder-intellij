package com.lilbaek.recordbuilder.processor.handler;

import com.intellij.psi.PsiField;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.PsiModifier;
import com.lilbaek.recordbuilder.psi.RLightFieldBuilder;
import com.lilbaek.recordbuilder.psi.RLightMethodBuilder;
import com.lilbaek.recordbuilder.util.PsiMethodUtil;
import org.jetbrains.annotations.NotNull;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

class BuilderElement implements BuilderElementHandler {
    BuilderElement() {
    }

    @Override
    public Collection<PsiField> renderBuilderFields(@NotNull BuilderInfo info) {
        Collection<PsiField> result = new ArrayList<>();
        result.add(new RLightFieldBuilder(info.getManager(), info.renderFieldName(), info.getFieldType())
                        .withContainingClass(info.getBuilderClass())
                        .withModifier(PsiModifier.PRIVATE)
                        .withNavigationElement(info.getVariable()));
        return result;
    }

    @Override
    public Collection<PsiMethod> renderBuilderMethod(@NotNull BuilderInfo info) {
        final String blockText = getAllMethodBody(info);
        final String methodName = calcBuilderMethodName(info);
        final RLightMethodBuilder methodBuilder = new RLightMethodBuilder(info.getManager(), methodName)
                        .withContainingClass(info.getBuilderClass())
                        .withMethodReturnType(info.getBuilderType())
                        .withParameter(info.getFieldName(), info.getFieldType())
                        .withNavigationElement(info.getVariable())
                        .withModifier(info.getVisibilityModifier())
                        .withAnnotations(info.getAnnotations());
        methodBuilder.withBody(PsiMethodUtil.createCodeBlockFromText(blockText, methodBuilder));
        return Collections.singleton(methodBuilder);
    }

    private String getAllMethodBody(@NotNull BuilderInfo info) {
        StringBuilder codeBlockTemplate = new StringBuilder("this.{0} = {1};\n");
        codeBlockTemplate.append("return {3};");
        return MessageFormat.format(codeBlockTemplate.toString(), info.renderFieldName(), info.getFieldName(),
                        info.renderFieldDefaultSetName(), info.getBuilderChainResult());
    }

    @Override
    public String renderBuildPrepare(@NotNull BuilderInfo info) {
        return "";
    }
}
