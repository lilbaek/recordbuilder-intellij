package com.lilbaek.recordbuilder.psi;

import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiManager;
import com.intellij.psi.SyntheticElement;
import com.intellij.psi.impl.light.LightIdentifier;

import java.util.Objects;

public class RLightIdentifier extends LightIdentifier implements SyntheticElement {
    private String myText;

    public RLightIdentifier(PsiManager manager, String text) {
        super(manager, text);
        myText = text;
    }

    @Override
    public String getText() {
        return myText;
    }

    public void setText(String text) {
        myText = text;
    }

    @Override
    public PsiElement copy() {
        return new LightIdentifier(getManager(), getText());
    }

    @Override
    public TextRange getTextRange() {
        TextRange r = super.getTextRange();
        return r == null ? TextRange.EMPTY_RANGE : r;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        RLightIdentifier that = (RLightIdentifier) o;

        return Objects.equals(myText, that.myText);
    }

    @Override
    public int hashCode() {
        return Objects.hash(myText);
    }
}
