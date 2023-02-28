package com.overklok.scopelabel.preferences;

import com.overklok.scopelabel.utils.UtilsColor;
import com.overklok.scopelabel.utils.UtilsFont;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;

import com.intellij.openapi.project.Project;
import com.intellij.util.xmlb.XmlSerializerUtil;
import com.intellij.util.xmlb.annotations.OptionTag;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.util.HashMap;
import java.util.Map;

@State(
        name = "ScopeLabel",
        storages = {
                @Storage("scope-label.xml"),
        }
)
public class ProjectPreferences implements PersistentStateComponent<ProjectPreferences> {
    @OptionTag
    private Map<String, ScopePrefs> items = new HashMap<>();

    @OptionTag
    private String label = "";

    @OptionTag
    private String backgroundColor = "#B12F2F";

    @OptionTag
    private String textColor = "#FFFFFF";

    @OptionTag
    private String fontSize = null;

    @OptionTag
    private String fontName = null;

    @OptionTag
    private Map<String, ScopePrefs> scopes = new HashMap<>();

    public static ProjectPreferences getInstance(Project project) {
        return ServiceManager.getService(project, ProjectPreferences.class);
    }

    @Nullable
    @Override
    public ProjectPreferences getState() {
        return this;
    }

    @Override
    public void loadState(@NotNull ProjectPreferences state) {
        XmlSerializerUtil.copyBean(state, this);
    }

    public void setBackgroundColor(Color color) {
        this.backgroundColor = UtilsColor.toHex(color);
    }

    public Map<String, ScopePrefs> getScopePrefs() {
        return scopes;
    }

    public Color getBackgroundColor() {
        return Color.decode(this.backgroundColor);
    }

    public void setTextColor(Color color) {
        this.textColor = UtilsColor.toHex(color);
    }

    public Color getTextColor() {
        return Color.decode(this.textColor);
    }

    public String getLabel() {
        return this.label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public int getFontSize(String scopeId) {
        System.out.println("get fs");
        System.out.println(scopeId);
        System.out.println(this.scopes.getOrDefault(scopeId, new ScopePrefs()).getFontSize());
        return Integer.parseInt(this.scopes.getOrDefault(scopeId, new ScopePrefs()).getFontSize());
    }

    public void setFontSize(String scopeId, int fontSize) {
        ScopePrefs scopePrefs = this.scopes.getOrDefault(scopeId, new ScopePrefs());
        scopePrefs.setFontSize(fontSize == -1 ? null : Integer.toString(fontSize));
        System.out.println("set fs");
        System.out.println(scopeId);
        System.out.println(fontSize);
        System.out.println(scopePrefs);
        this.scopes.put(scopeId, scopePrefs);
    }

    public String getFontName() {
        return fontName == null ? "" : fontName;
    }

    public Font getFont() {
        return UtilsFont.getFontByName(fontName);
    }

    public void setFontName(String font) {
        this.fontName = font.isEmpty() ? null : font;
    }
}
