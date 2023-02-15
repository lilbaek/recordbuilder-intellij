package com.lilbaek.recordbuilder.util;

import com.intellij.psi.PsiAnnotation;
import com.intellij.psi.PsiModifier;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class ProcessorUtil {
    @NonNls
    private static final String ACCESS_LEVEL_PUBLIC = "PUBLIC";

    @Nullable
    @PsiModifier.ModifierConstant
    public static String getAccessVisibility(@NotNull PsiAnnotation psiAnnotation) {
        return getLevelVisibility(psiAnnotation, "access");
    }

    @Nullable
    @PsiModifier.ModifierConstant
    private static String getLevelVisibility(@NotNull PsiAnnotation psiAnnotation, @NotNull String parameter) {
        return convertAccessLevelToJavaModifier(PsiAnnotationUtil.getEnumAnnotationValue(psiAnnotation, parameter, ACCESS_LEVEL_PUBLIC));
    }

    @Nullable
    @PsiModifier.ModifierConstant
    private static String convertAccessLevelToJavaModifier(String value) {
        if (null == value || value.isEmpty()) {
            return PsiModifier.PUBLIC;
        }

        if ("PUBLIC".equals(value)) {
            return PsiModifier.PUBLIC;
        }
        if ("MODULE".equals(value)) {
            return PsiModifier.PACKAGE_LOCAL;
        }
        if ("PROTECTED".equals(value)) {
            return PsiModifier.PROTECTED;
        }
        if ("PACKAGE".equals(value)) {
            return PsiModifier.PACKAGE_LOCAL;
        }
        if ("PRIVATE".equals(value)) {
            return PsiModifier.PRIVATE;
        }
        if ("NONE".equals(value)) {
            return null;
        }
        return null;
    }
}
