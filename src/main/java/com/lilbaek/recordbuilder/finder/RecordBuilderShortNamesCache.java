package com.lilbaek.recordbuilder.finder;

import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiField;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.search.PsiShortNamesCache;
import com.intellij.util.Processor;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public class RecordBuilderShortNamesCache extends PsiShortNamesCache {
    public static final String BUILDER_SUFFIX = "Builder";

    @Override
    public @NotNull PsiClass @NotNull [] getClassesByName(@NotNull @NonNls final String qualifiedName, @NotNull final GlobalSearchScope scope) {
        // System.out.println(qualifiedName);;
        if (!qualifiedName.endsWith(BUILDER_SUFFIX)) {
            return new PsiClass[0];
        }
        final String factoryClassName = getClassName(qualifiedName);
        final PsiClass[] classesByName = PsiShortNamesCache.getInstance(scope.getProject())
                        .getClassesByName(factoryClassName, scope);
        if (classesByName.length > 0) {
            final PsiClass psiClass = classesByName[0];
            if (!psiClass.isRecord()) {
                return new PsiClass[0];
            }
            final Optional<PsiClass> result = Invoker.getPsiClass(psiClass);
            if (result.isPresent()) {
                return new PsiClass[] { result.get() };
            }
        }
        return new PsiClass[0];
    }

    private String getClassName(@NotNull final String qualifiedName) {
        return qualifiedName.substring(0, qualifiedName.lastIndexOf(BUILDER_SUFFIX));
    }

    @Override
    public @NotNull String @NotNull [] getAllClassNames() {
        return new String[0];
    }

    @Override
    public @NotNull PsiMethod @NotNull [] getMethodsByName(@NonNls @NotNull final String name, @NotNull final GlobalSearchScope scope) {
        return new PsiMethod[0];
    }

    @Override
    public @NotNull PsiMethod @NotNull [] getMethodsByNameIfNotMoreThan(@NonNls @NotNull final String name, @NotNull final GlobalSearchScope scope,
                    final int maxCount) {
        return new PsiMethod[0];
    }

    @Override
    public @NotNull PsiField @NotNull [] getFieldsByNameIfNotMoreThan(@NonNls @NotNull final String name, @NotNull final GlobalSearchScope scope,
                    final int maxCount) {
        return new PsiField[0];
    }

    @Override
    public boolean processMethodsWithName(@NonNls @NotNull final String name, @NotNull final GlobalSearchScope scope,
                    @NotNull final Processor<? super PsiMethod> processor) {
        return false;
    }

    @Override
    public @NotNull String @NotNull [] getAllMethodNames() {
        return new String[0];
    }

    @Override
    public @NotNull PsiField @NotNull [] getFieldsByName(@NotNull @NonNls final String name, @NotNull final GlobalSearchScope scope) {
        return new PsiField[0];
    }

    @Override
    public @NotNull String @NotNull [] getAllFieldNames() {
        return new String[0];
    }
}
