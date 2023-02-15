package com.lilbaek.recordbuilder.psi;

import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiClassType;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiField;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.PsiModifier;
import com.intellij.psi.PsiTypeParameter;
import com.intellij.psi.PsiTypeParameterList;
import com.intellij.psi.SyntheticElement;
import com.intellij.psi.impl.light.LightPsiClassBuilder;
import com.intellij.psi.impl.source.PsiExtensibleClass;
import com.lilbaek.recordbuilder.icon.Icons;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.Icon;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Stream;

// TODO
public class RLightClassBuilder extends LightPsiClassBuilder implements PsiExtensibleClass, SyntheticElement {

    private final String myQualifiedName;
    private final Icon myBaseIcon;
    private final RLightModifierList myModifierList;

    private boolean myIsEnum;
    private PsiField[] myFields;
    private PsiMethod[] myMethods;

    private Supplier<? extends Collection<PsiField>> fieldSupplier = Collections::emptyList;
    private Supplier<? extends Collection<PsiMethod>> methodSupplier = Collections::emptyList;

    public RLightClassBuilder(@NotNull PsiElement context, @NotNull String simpleName, @NotNull String qualifiedName) {
        super(context, simpleName);
        myIsEnum = false;
        myQualifiedName = qualifiedName;
        myBaseIcon = Icons.CLASS_ICON;
        myModifierList = new RLightModifierList(context.getManager(), context.getLanguage());
    }

    @NotNull
    @Override
    public RLightModifierList getModifierList() {
        return myModifierList;
    }

    @Override
    public PsiElement getScope() {
        if (getContainingClass() != null) {
            return getContainingClass().getScope();
        }
        return super.getScope();
    }

    @Override
    public PsiElement getParent() {
        return getContainingClass();
    }

    @Nullable
    @Override
    public String getQualifiedName() {
        return myQualifiedName;
    }

    @Override
    public Icon getElementIcon(final int flags) {
        return myBaseIcon;
    }

    @Override
    public TextRange getTextRange() {
        TextRange r = super.getTextRange();
        return r == null ? TextRange.EMPTY_RANGE : r;
    }

    @Override
    public PsiFile getContainingFile() {
        if (null != getContainingClass()) {
            return getContainingClass().getContainingFile();
        }
        return super.getContainingFile();
    }

    @Override
    public boolean isEnum() {
        return myIsEnum;
    }

    @Override
    public PsiField @NotNull [] getFields() {
        if (null == myFields) {
            Collection<PsiField> generatedFields = fieldSupplier.get();
            myFields = generatedFields.toArray(PsiField.EMPTY_ARRAY);
            fieldSupplier = Collections::emptyList;
        }
        return myFields;
    }

    @Override
    public PsiMethod @NotNull [] getMethods() {
        if (null == myMethods) {
            Collection<PsiMethod> generatedMethods = methodSupplier.get();
            myMethods = generatedMethods.toArray(PsiMethod.EMPTY_ARRAY);
            methodSupplier = Collections::emptyList;
        }
        return myMethods;
    }

    @Override
    public @NotNull List<PsiField> getOwnFields() {
        return Collections.emptyList();
    }

    @Override
    public @NotNull List<PsiMethod> getOwnMethods() {
        return Collections.emptyList();
    }

    @Override
    public @NotNull List<PsiClass> getOwnInnerClasses() {
        return Collections.emptyList();
    }

    public RLightClassBuilder withFieldSupplier(final Supplier<? extends Collection<PsiField>> fieldSupplier) {
        this.fieldSupplier = fieldSupplier;
        return this;
    }

    public RLightClassBuilder withMethodSupplier(final Supplier<? extends Collection<PsiMethod>> methodSupplier) {
        this.methodSupplier = methodSupplier;
        return this;
    }

    public RLightClassBuilder withEnum(boolean isEnum) {
        myIsEnum = isEnum;
        return this;
    }

    public RLightClassBuilder withImplicitModifier(@PsiModifier.ModifierConstant @NotNull @NonNls String modifier) {
        myModifierList.addImplicitModifierProperty(modifier);
        return this;
    }

    public RLightClassBuilder withModifier(@PsiModifier.ModifierConstant @NotNull @NonNls String modifier) {
        myModifierList.addModifier(modifier);
        return this;
    }

    public RLightClassBuilder withContainingClass(@NotNull PsiClass containingClass) {
        setContainingClass(containingClass);
        return this;
    }

    public RLightClassBuilder withNavigationElement(PsiElement navigationElement) {
        setNavigationElement(navigationElement);
        return this;
    }

    public RLightClassBuilder withExtends(PsiClassType baseClassType) {
        getExtendsList().addReference(baseClassType);
        return this;
    }

    public RLightClassBuilder withParameterTypes(@Nullable PsiTypeParameterList parameterList) {
        if (parameterList != null) {
            Stream.of(parameterList.getTypeParameters()).forEach(this::withParameterType);
        }
        return this;
    }

    public RLightClassBuilder withParameterType(@NotNull PsiTypeParameter psiTypeParameter) {
        getTypeParameterList().addParameter(psiTypeParameter);
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        RLightClassBuilder that = (RLightClassBuilder)o;

        return myQualifiedName.equals(that.myQualifiedName);
    }

    @Override
    public int hashCode() {
        return myQualifiedName.hashCode();
    }
}
