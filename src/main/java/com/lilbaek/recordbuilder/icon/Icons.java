package com.lilbaek.recordbuilder.icon;

import com.intellij.openapi.util.IconLoader;

import javax.swing.Icon;

public class Icons {
    private static Icon load(String path) {
        return IconLoader.getIcon(path, Icons.class);
    }

    public static final Icon CLASS_ICON = load("/icons/nodes/class.svg");
    public static final Icon FIELD_ICON = load("/icons/nodes/field.svg");
    public static final Icon METHOD_ICON = load("/icons/nodes/method.svg");
}
