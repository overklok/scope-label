package com.overklok.scopelabel.preferences;

import com.intellij.util.xmlb.annotations.OptionTag;
import com.jgoodies.common.bean.Bean;

import javax.annotation.Nullable;

public class ScopePrefs extends Bean {
    @OptionTag
    private String label = "";

    @OptionTag
    private String fontSize = "8";

    @OptionTag
    private String backgroundColor = "#B12F2F";

    @OptionTag
    private String textColor = "#FFFFFF";

    @OptionTag
    private String fontName = null;


    public void setLabel(String label) {
        this.label = label;
    }

    public String getLabel() {
        return this.label;
    }

    public void setFontSize(String fontSize) {
        this.fontSize = fontSize;
    }

    public String getFontSize() {
        return fontSize;
    }

    public void setFontName(String fontName) {
        this.fontName = fontName;
    }

    public String getFontName() {
        return this.fontName;
    }

    public void setBackgroundColor(String color) {
        this.backgroundColor = color;
    }

    public String getBackgroundColor() {
        return this.backgroundColor;
    }

    public void setTextColor(String color) {
        this.textColor = color;
    }

    public String getTextColor() {
        return this.textColor;
    }
}
