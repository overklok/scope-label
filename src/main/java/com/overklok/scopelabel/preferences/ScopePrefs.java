package com.overklok.scopelabel.preferences;

import com.intellij.util.xmlb.annotations.OptionTag;
import com.jgoodies.common.bean.Bean;

import javax.annotation.Nullable;

public class ScopePrefs extends Bean {
    @OptionTag
    private String label = "";

    @OptionTag
    private String fontSize = "8";

    @Nullable
    void setFontSize(String size) {
        fontSize = size;
    }

    public String getFontSize() {
        return fontSize;
    }
}
