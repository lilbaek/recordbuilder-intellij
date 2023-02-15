package com.lilbaek.recordbuilder.processor.handler;

import com.intellij.psi.CommonClassNames;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiExpression;
import com.intellij.psi.PsiField;
import com.intellij.psi.PsiManager;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.PsiModifier;
import com.intellij.psi.PsiModifierList;
import com.intellij.psi.PsiModifierListOwner;
import com.intellij.psi.PsiParameter;
import com.intellij.psi.PsiSubstitutor;
import com.intellij.psi.PsiType;
import com.intellij.psi.PsiVariable;
import com.intellij.psi.impl.PsiImplUtil;
import com.lilbaek.recordbuilder.processor.field.AccessorsInfo;
import com.lilbaek.recordbuilder.util.PsiAnnotationSearchUtil;
import com.lilbaek.recordbuilder.util.PsiClassUtil;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Collections;

public class BuilderInfo {
    private PsiVariable variableInClass;
    private PsiType fieldInBuilderType;
    private boolean deprecated;
    private String visibilityModifier;
    private String setterPrefix;
    private String builderChainResult = "this";
    private PsiClass builderClass;
    private PsiType builderClassType;
    private String fieldInBuilderName;
    private PsiExpression fieldInitializer;
    private BuilderElementHandler builderElementHandler;
    private String instanceVariableName = "this";

    public static BuilderInfo fromPsiParameter(@NotNull PsiParameter psiParameter) {
        final BuilderInfo result = new BuilderInfo();

        result.variableInClass = psiParameter;
        result.fieldInBuilderType = psiParameter.getType();
        result.deprecated = hasDeprecatedAnnotation(psiParameter);
        result.fieldInitializer = null;
        result.fieldInBuilderName = psiParameter.getName();
        result.builderElementHandler = new BuilderElement();

        return result;
    }

    private static boolean hasDeprecatedAnnotation(@NotNull PsiModifierListOwner modifierListOwner) {
        return PsiAnnotationSearchUtil.isAnnotatedWith(modifierListOwner, Deprecated.class.getName());
    }

    public static BuilderInfo fromPsiField(@NotNull PsiField psiField) {
        final BuilderInfo result = new BuilderInfo();

        result.variableInClass = psiField;
        result.deprecated = isDeprecated(psiField);
        result.fieldInBuilderType = psiField.getType();
        result.fieldInitializer = psiField.getInitializer();
        final AccessorsInfo accessorsInfo = AccessorsInfo.build(psiField);
        result.fieldInBuilderName = accessorsInfo.removePrefix(psiField.getName());
        result.builderElementHandler = new BuilderElement();

        return result;
    }

    private static boolean isDeprecated(@NotNull PsiField psiField) {
        return PsiImplUtil.isDeprecatedByDocTag(psiField) || hasDeprecatedAnnotation(psiField);
    }

    public BuilderInfo withSubstitutor(@NotNull PsiSubstitutor builderSubstitutor) {
        fieldInBuilderType = builderSubstitutor.substitute(fieldInBuilderType);
        return this;
    }

    public BuilderInfo withVisibilityModifier(String visibilityModifier) {
        this.visibilityModifier = visibilityModifier;
        return this;
    }

    public BuilderInfo withSetterPrefix(String setterPrefix) {
        this.setterPrefix = setterPrefix;
        return this;
    }

    public BuilderInfo withBuilderClass(@NotNull PsiClass builderClass) {
        this.builderClass = builderClass;
        this.builderClassType = PsiClassUtil.getTypeWithGenerics(builderClass);
        return this;
    }

    public boolean useForBuilder() {
        boolean result = true;

        PsiModifierList modifierList = variableInClass.getModifierList();
        if (null != modifierList) {
            //Skip static fields.
            result = !modifierList.hasModifierProperty(PsiModifier.STATIC);

            // skip initialized final fields unless annotated with @Builder.Default
            final boolean isInitializedFinalField = null != fieldInitializer && modifierList.hasModifierProperty(PsiModifier.FINAL);
            if (isInitializedFinalField) {
                result = false;
            }
        }
        return result;
    }

    public PsiManager getManager() {
        return variableInClass.getManager();
    }

    public String getFieldName() {
        return fieldInBuilderName;
    }

    public PsiType getFieldType() {
        return fieldInBuilderType;
    }

    public PsiVariable getVariable() {
        return variableInClass;
    }
    @PsiModifier.ModifierConstant
    public String getVisibilityModifier() {
        return visibilityModifier;
    }

    public String getSetterPrefix() {
        return setterPrefix;
    }

    public PsiClass getBuilderClass() {
        return builderClass;
    }

    public PsiType getBuilderType() {
        return builderClassType;
    }

    public String getBuilderChainResult() {
        return builderChainResult;
    }
    public Collection<String> getAnnotations() {
        if (deprecated) {
            return Collections.singleton(CommonClassNames.JAVA_LANG_DEPRECATED);
        }
        return Collections.emptyList();
    }

    public Collection<PsiField> renderBuilderFields() {
        return builderElementHandler.renderBuilderFields(this);
    }

    private String calcBuilderMethodName() {
        return builderElementHandler.calcBuilderMethodName(this);
    }

    public Collection<PsiMethod> renderBuilderMethods() {
        return builderElementHandler.renderBuilderMethod(this);
    }

    public String renderBuildPrepare() {
        return builderElementHandler.renderBuildPrepare(this);
    }

    public String renderBuildCall() {
        return renderFieldName();
    }

    public String renderFieldName() {
        return fieldInBuilderName;
    }

    public String renderFieldDefaultSetName() {
        return null;
    }

    public String renderFieldDefaultProviderName() {
        return null;
    }
}
