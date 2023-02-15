package com.lilbaek.recordbuilder.processor.handler;

import com.intellij.psi.PsiAnnotation;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.PsiModifier;
import com.intellij.psi.PsiNameHelper;
import com.intellij.psi.PsiParameter;
import com.intellij.psi.PsiReferenceList;
import com.intellij.psi.PsiSubstitutor;
import com.intellij.psi.PsiType;
import com.intellij.psi.PsiTypeParameter;
import com.intellij.psi.PsiTypeParameterListOwner;
import com.lilbaek.recordbuilder.psi.RLightClassBuilder;
import com.lilbaek.recordbuilder.psi.RLightMethodBuilder;
import com.lilbaek.recordbuilder.util.ProcessorUtil;
import com.lilbaek.recordbuilder.util.PsiClassUtil;
import com.lilbaek.recordbuilder.util.PsiMethodUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.intellij.openapi.util.text.StringUtil.capitalize;
import static com.intellij.openapi.util.text.StringUtil.replace;

public class BuilderHandler {
    private final static String BUILD_METHOD_NAME = "build";
    private final static String BUILDER_METHOD_NAME = "builder";

    PsiSubstitutor getBuilderSubstitutor(@NotNull PsiTypeParameterListOwner classOrMethodToBuild, @NotNull PsiClass innerClass) {
        PsiSubstitutor substitutor = PsiSubstitutor.EMPTY;
        if (innerClass.hasModifierProperty(PsiModifier.STATIC)) {
            PsiTypeParameter[] typeParameters = classOrMethodToBuild.getTypeParameters();
            PsiTypeParameter[] builderParams = innerClass.getTypeParameters();
            if (typeParameters.length <= builderParams.length) {
                for (int i = 0; i < typeParameters.length; i++) {
                    PsiTypeParameter typeParameter = typeParameters[i];
                    substitutor = substitutor.put(typeParameter, PsiSubstitutor.EMPTY.substitute(builderParams[i]));
                }
            }
        }
        return substitutor;
    }

    @NotNull
    public PsiClass createBuilderClass(@NotNull PsiClass psiClass, @Nullable PsiMethod psiMethod, @NotNull PsiAnnotation psiAnnotation) {
        final RLightClassBuilder builderClass;
        if (null != psiMethod) {
            builderClass = createEmptyBuilderClass(psiClass, psiMethod, psiAnnotation);
        } else {
            builderClass = createEmptyBuilderClass(psiClass, psiAnnotation);
        }

        builderClass.withFieldSupplier(() -> {
            final List<BuilderInfo> builderInfos = createBuilderInfos(psiAnnotation, psiClass, psiMethod, builderClass);
            // create builder Fields
            return builderInfos.stream()
                            .map(BuilderInfo::renderBuilderFields)
                            .flatMap(Collection::stream)
                            .collect(Collectors.toList());
        });

        builderClass.withMethodSupplier(() -> {
            Collection<PsiMethod> psiMethods = new ArrayList<>();
            try {
                final List<BuilderInfo> builderInfos = createBuilderInfos(psiAnnotation, psiClass, psiMethod, builderClass);
                // create builder methods
                builderInfos.stream()
                                .map(BuilderInfo::renderBuilderMethods)
                                .forEach(psiMethods::addAll);

                // create 'build' method
                final String buildMethodName = getBuildMethodName();
                psiMethods.add(createBuildMethod(psiAnnotation, psiClass, psiMethod, builderClass, buildMethodName, builderInfos));

                // create 'builder' method
                final String builderMethodName = getBuilderMethodName();
                psiMethods.add(createBuilderMethod(psiAnnotation, psiClass, psiMethod, builderClass, builderMethodName, builderInfos));
            } catch (Exception e) {
                System.out.println(e);
            }
            return psiMethods;
        });

        return builderClass;
    }

    public Optional<PsiClass> getExistInnerBuilderClass(@NotNull PsiClass psiClass, @Nullable PsiMethod psiMethod, @NotNull PsiAnnotation psiAnnotation) {
        final String builderClassName = getBuilderClassName(psiClass, psiAnnotation, psiMethod);
        return PsiClassUtil.getInnerClassInternByName(psiClass, builderClassName);
    }

    PsiType getReturnTypeOfBuildMethod(@NotNull PsiClass psiClass, @Nullable PsiMethod psiMethod) {
        final PsiType result;
        if (null == psiMethod || psiMethod.isConstructor()) {
            result = PsiClassUtil.getTypeWithGenerics(psiClass);
        } else {
            result = psiMethod.getReturnType();
        }
        return result;
    }

    @NotNull
    public String getBuildMethodName() {
        return BUILD_METHOD_NAME;
    }

    @NotNull
    public String getBuilderMethodName() {
        return BUILDER_METHOD_NAME;
    }

    @NotNull
    @PsiModifier.ModifierConstant
    private String getBuilderOuterAccessVisibility(@NotNull PsiAnnotation psiAnnotation) {
        final String accessVisibility = ProcessorUtil.getAccessVisibility(psiAnnotation);
        return null == accessVisibility ? PsiModifier.PUBLIC : accessVisibility;
    }

    @NotNull
    @PsiModifier.ModifierConstant
    private String getBuilderInnerAccessVisibility(@NotNull PsiAnnotation psiAnnotation) {
        final String accessVisibility = getBuilderOuterAccessVisibility(psiAnnotation);
        return PsiModifier.PROTECTED.equals(accessVisibility) ? PsiModifier.PUBLIC : accessVisibility;
    }

    @NotNull
    public String getBuilderClassName(@NotNull PsiClass psiClass, @NotNull PsiAnnotation psiAnnotation, @Nullable PsiMethod psiMethod) {
        String relevantReturnType = psiClass.getName();
        if (null != psiMethod && !psiMethod.isConstructor()) {
            final PsiType psiMethodReturnType = psiMethod.getReturnType();
            if (null != psiMethodReturnType) {
                relevantReturnType = PsiNameHelper.getQualifiedClassName(psiMethodReturnType.getPresentableText(), false);
            }
        }

        return getBuilderClassName(relevantReturnType);
    }

    @NotNull
    String getBuilderClassName(String returnTypeName) {
        final String builderClassNamePattern = "*Builder";
        return replace(builderClassNamePattern, "*", capitalize(returnTypeName));
    }

    @NotNull
    private Stream<BuilderInfo> createBuilderInfos(@NotNull PsiClass psiClass, @Nullable PsiMethod psiClassMethod) {
        final Stream<BuilderInfo> result;
        if (null != psiClassMethod) {
            result = Arrays.stream(psiClassMethod.getParameterList().getParameters()).map(BuilderInfo::fromPsiParameter);
        } else {
            result = PsiClassUtil.collectClassFieldsIntern(psiClass).stream().map(BuilderInfo::fromPsiField)
                            .filter(BuilderInfo::useForBuilder);
        }
        return result;
    }

    public List<BuilderInfo> createBuilderInfos(@NotNull PsiAnnotation psiAnnotation, @NotNull PsiClass psiClass,
                    @Nullable PsiMethod psiClassMethod, @NotNull PsiClass builderClass) {
        final PsiSubstitutor builderSubstitutor = getBuilderSubstitutor(psiClass, builderClass);
        final String accessVisibility = getBuilderInnerAccessVisibility(psiAnnotation);
        final var builderInfos = createBuilderInfos(psiClass, psiClassMethod).toList();
        return builderInfos.stream()
                        .map(info -> info.withSubstitutor(builderSubstitutor))
                        .map(info -> info.withBuilderClass(builderClass))
                        .map(info -> info.withVisibilityModifier(accessVisibility))
                        .map(info -> info.withSetterPrefix(""))
                        .collect(Collectors.toList());
    }


    @NotNull
    private RLightClassBuilder createEmptyBuilderClass(@NotNull PsiClass psiClass, @NotNull PsiMethod psiMethod, @NotNull PsiAnnotation psiAnnotation) {
        return createBuilderClass(psiClass, psiMethod,
                        psiMethod.isConstructor() || psiMethod.hasModifierProperty(PsiModifier.STATIC), psiAnnotation);
    }

    @NotNull
    private RLightClassBuilder createEmptyBuilderClass(@NotNull PsiClass psiClass, @NotNull PsiAnnotation psiAnnotation) {
        return createBuilderClass(psiClass, psiClass, true, psiAnnotation);
    }

    public Optional<PsiClass> createBuilderClassIfNotExist(@NotNull PsiClass psiClass, @Nullable PsiMethod psiMethod, @NotNull PsiAnnotation psiAnnotation) {
        PsiClass builderClass = null;
        if (getExistInnerBuilderClass(psiClass, psiMethod, psiAnnotation).isEmpty()) {
            builderClass = createBuilderClass(psiClass, psiMethod, psiAnnotation);
        }
        return Optional.ofNullable(builderClass);
    }

    @NotNull
    private RLightClassBuilder createBuilderClass(@NotNull PsiClass psiClass, @NotNull PsiTypeParameterListOwner psiTypeParameterListOwner, final boolean isStatic, @NotNull PsiAnnotation psiAnnotation) {
        PsiMethod psiMethod = null;
        if (psiTypeParameterListOwner instanceof PsiMethod) {
            psiMethod = (PsiMethod) psiTypeParameterListOwner;
        }

        final String builderClassName = getBuilderClassName(psiClass, psiAnnotation, psiMethod);
        final String builderClassQualifiedName = builderClassName;

        final RLightClassBuilder classBuilder = new RLightClassBuilder(psiClass, builderClassName, builderClassQualifiedName)
                        .withNavigationElement(psiAnnotation)
                        .withParameterTypes((null != psiMethod && psiMethod.isConstructor()) ? psiClass.getTypeParameterList() : psiTypeParameterListOwner.getTypeParameterList())
                        .withModifier(getBuilderOuterAccessVisibility(psiAnnotation));
        return classBuilder;
    }

    @NotNull
    public PsiMethod createBuilderMethod(@NotNull PsiAnnotation psiAnnotation, @NotNull PsiClass parentClass, @Nullable PsiMethod psiMethod, @NotNull PsiClass builderClass,
                    @NotNull String buildMethodName, List<BuilderInfo> builderInfos) {

        final PsiType builderType = getReturnTypeOfBuildMethod(builderClass, psiMethod);

        final PsiSubstitutor builderSubstitutor = getBuilderSubstitutor(parentClass, builderClass);
        final PsiType returnType = builderSubstitutor.substitute(builderType);

        final String buildMethodPrepare = builderInfos.stream()
                        .map(BuilderInfo::renderBuildPrepare)
                        .collect(Collectors.joining());

        final String buildMethodParameters = builderInfos.stream()
                        .map(BuilderInfo::renderBuildCall)
                        .collect(Collectors.joining(","));

        final RLightMethodBuilder methodBuilder = new RLightMethodBuilder(parentClass.getManager(), buildMethodName)
                        .withMethodReturnType(returnType)
                        .withContainingClass(builderClass)
                        .withNavigationElement(parentClass)
                        .withModifier(PsiModifier.STATIC);
        final String codeBlockText = createBuildMethodCodeBlockText(psiMethod, builderClass, returnType, buildMethodPrepare, buildMethodParameters);
        methodBuilder.withBody(PsiMethodUtil.createCodeBlockFromText(codeBlockText, methodBuilder));

        Optional<PsiMethod> definedConstructor = Optional.ofNullable(psiMethod);
        if (definedConstructor.isEmpty()) {
            final Collection<PsiMethod> classConstructors = PsiClassUtil.collectClassConstructorIntern(parentClass);
            definedConstructor = classConstructors.stream()
                            .filter(m -> sameParameters(m.getParameterList().getParameters(), builderInfos))
                            .findFirst();
        }
        definedConstructor.map(PsiMethod::getThrowsList).map(PsiReferenceList::getReferencedTypes).map(Arrays::stream)
                        .ifPresent(stream -> stream.forEach(methodBuilder::withException));

        return methodBuilder;
    }

    @NotNull
    public PsiMethod createBuildMethod(@NotNull PsiAnnotation psiAnnotation, @NotNull PsiClass parentClass, @Nullable PsiMethod psiMethod, @NotNull PsiClass builderClass,
                    @NotNull String buildMethodName, List<BuilderInfo> builderInfos) {
        final PsiType builderType = getReturnTypeOfBuildMethod(parentClass, psiMethod);

        final PsiSubstitutor builderSubstitutor = getBuilderSubstitutor(parentClass, builderClass);
        final PsiType returnType = builderSubstitutor.substitute(builderType);

        final String buildMethodPrepare = builderInfos.stream()
                        .map(BuilderInfo::renderBuildPrepare)
                        .collect(Collectors.joining());

        final String buildMethodParameters = builderInfos.stream()
                        .map(BuilderInfo::renderBuildCall)
                        .collect(Collectors.joining(","));

        final RLightMethodBuilder methodBuilder = new RLightMethodBuilder(parentClass.getManager(), buildMethodName)
                        .withMethodReturnType(returnType)
                        .withContainingClass(builderClass)
                        .withNavigationElement(parentClass)
                        .withModifier(getBuilderInnerAccessVisibility(psiAnnotation));
        final String codeBlockText = createBuildMethodCodeBlockText(psiMethod, builderClass, returnType, buildMethodPrepare, buildMethodParameters);
        methodBuilder.withBody(PsiMethodUtil.createCodeBlockFromText(codeBlockText, methodBuilder));

        Optional<PsiMethod> definedConstructor = Optional.ofNullable(psiMethod);
        if (definedConstructor.isEmpty()) {
            final Collection<PsiMethod> classConstructors = PsiClassUtil.collectClassConstructorIntern(parentClass);
            definedConstructor = classConstructors.stream()
                            .filter(m -> sameParameters(m.getParameterList().getParameters(), builderInfos))
                            .findFirst();
        }
        definedConstructor.map(PsiMethod::getThrowsList).map(PsiReferenceList::getReferencedTypes).map(Arrays::stream)
                        .ifPresent(stream -> stream.forEach(methodBuilder::withException));

        return methodBuilder;
    }

    private boolean sameParameters(PsiParameter[] parameters, List<BuilderInfo> builderInfos) {
        if (parameters.length != builderInfos.size()) {
            return false;
        }

        final Iterator<BuilderInfo> builderInfoIterator = builderInfos.iterator();
        for (PsiParameter psiParameter : parameters) {
            final BuilderInfo builderInfo = builderInfoIterator.next();
            if (!psiParameter.getType().isAssignableFrom(builderInfo.getFieldType())) {
                return false;
            }
        }
        return true;
    }

    @NotNull
    private String createBuildMethodCodeBlockText(@Nullable PsiMethod psiMethod, @NotNull PsiClass psiClass, @NotNull PsiType buildMethodReturnType,
                    @NotNull String buildMethodPrepare, @NotNull String buildMethodParameters) {
        final String blockText;

        final String codeBlockFormat, callExpressionText;

        if (null == psiMethod || psiMethod.isConstructor()) {
            codeBlockFormat = "%s\n return new %s(%s);";
            callExpressionText = buildMethodReturnType.getPresentableText();
        } else {
            if (PsiType.VOID.equals(buildMethodReturnType)) {
                codeBlockFormat = "%s\n %s(%s);";
            } else {
                codeBlockFormat = "%s\n return %s(%s);";
            }
            callExpressionText = calculateCallExpressionForMethod(psiMethod, psiClass);
        }
        blockText = String.format(codeBlockFormat, buildMethodPrepare, callExpressionText, buildMethodParameters);
        return blockText;
    }

    @NotNull
    private String calculateCallExpressionForMethod(@NotNull PsiMethod psiMethod, @NotNull PsiClass builderClass) {
        final PsiClass containingClass = psiMethod.getContainingClass();

        StringBuilder className = new StringBuilder();
        if (null != containingClass) {
            className.append(containingClass.getName()).append(".");
            if (!psiMethod.isConstructor() && !psiMethod.hasModifierProperty(PsiModifier.STATIC)) {
                className.append("this.");
            }
            if (builderClass.hasTypeParameters()) {
                className.append(Arrays.stream(builderClass.getTypeParameters()).map(PsiTypeParameter::getName).collect(Collectors.joining(",", "<", ">")));
            }
        }
        return className + psiMethod.getName();
    }
}
