package com.lilbaek.recordbuilder.finder;

import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElementFinder;
import com.intellij.psi.PsiPackage;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.search.PsiShortNamesCache;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Optional;

public class RecordBuilderFinder extends PsiElementFinder {
    public static final String BUILDER_SUFFIX = "Builder";

    /*
    https://youtrack.jetbrains.com/issue/IDEA-236676?_ga=2.265246223.1616662009.1585842988-2137421544.1561313206&_gl=1*2dvbx3*_ga*NDk5MjUxMTY1LjE2MDI1NzIwMTc.*_ga_9J976DJZ68*MTY3NjMxNzQyMS4xMi4xLjE2NzYzMTg5NTEuMjEuMC4w
     */

    @Nullable
    @Override
    public PsiClass findClass(@NotNull final String qualifiedName, @NotNull final GlobalSearchScope scope) {
        if (!qualifiedName.endsWith(BUILDER_SUFFIX)) {
            return null;
        }
        return getPsiClass(qualifiedName, scope).orElse(null);
    }

    @NotNull
    @Override
    public PsiClass @NotNull [] findClasses(@NotNull final String qualifiedName, @NotNull final GlobalSearchScope globalSearchScope) {
        if (!qualifiedName.endsWith(BUILDER_SUFFIX)) {
            return PsiClass.EMPTY_ARRAY;
        }
        final Optional<PsiClass> psiClass = getPsiClass(qualifiedName, globalSearchScope);
        if (psiClass.isPresent()) {
            return new PsiClass[] { psiClass.get() };
        }
        return PsiClass.EMPTY_ARRAY;
    }

    @NotNull
    @Override
    public PsiClass @NotNull [] getClasses(@NotNull final PsiPackage psiPackage, @NotNull final GlobalSearchScope scope) {
        // TODO: Can this be optimized?
        final var result = new ArrayList<PsiClass>();
        final PsiShortNamesCache instance = PsiShortNamesCache.getInstance(scope.getProject());
        for (final String allClassName : instance.getAllClassNames()) {
            final PsiClass[] classesByName = instance.getClassesByName(allClassName, scope);
            if (classesByName.length > 0) {
                if (classesByName[0].isRecord()) {
                    final Optional<PsiClass> psiClass = getPsiClass(classesByName[0]);
                    if (psiClass.isPresent()) {
                        psiClass.ifPresent(result::add);
                    }
                }
            }

        }
        return result.toArray(new PsiClass[0]);
    }

    private Optional<PsiClass> getPsiClass(final @NotNull String qualifiedName, final @NotNull GlobalSearchScope scope) {
        final String lookup = getClassName(qualifiedName);
        final PsiClass[] classesByName = PsiShortNamesCache.getInstance(scope.getProject())
                        .getClassesByName(lookup, scope);
        if (classesByName.length > 0) {
            return getPsiClass(classesByName[0]);
        }
        return Optional.empty();
    }

    private Optional<PsiClass> getPsiClass(final PsiClass psiClass) {
        return Invoker.getPsiClass(psiClass);
    }


    private String getClassName(@NotNull final String qualifiedName) {
        //System.out.println("Qualified: " + qualifiedName);
        //System.out.println("Name: " + StringUtil.getShortName(qualifiedName));
        //System.out.println("Package: " + StringUtil.getPackageName(qualifiedName));
        final String shortName = StringUtil.getShortName(qualifiedName);
        return shortName.substring(0, shortName.lastIndexOf(BUILDER_SUFFIX));
    }
}
