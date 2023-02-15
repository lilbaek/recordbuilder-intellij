package com.lilbaek.recordbuilder.util;

import com.intellij.openapi.util.text.StringUtil;

public class Utils {

    public static String buildAccessorName(String prefix, String suffix) {
        if (prefix.isEmpty()) {
            return suffix;
        }
        if (suffix.isEmpty()) {
            return prefix;
        }
        return buildName(prefix, suffix);
    }

    private static String buildName(String prefix, String suffix) {
        return prefix + StringUtil.capitalize(suffix);
    }
}
