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

    public Map<String, ScopePrefs> getScopePrefs() {
        return scopes;
    }

    public void setBackgroundColor(String scopeId, Color color) {
        ScopePrefs scopePrefs = this.scopes.getOrDefault(scopeId, new ScopePrefs());
        scopePrefs.setBackgroundColor(UtilsColor.toHex(color));
    }

    public Color getBackgroundColor(String scopeId) {
        return Color.decode(this.scopes.getOrDefault(scopeId, new ScopePrefs()).getBackgroundColor());
    }

    public void setTextColor(String scopeId, Color color) {
        ScopePrefs scopePrefs = this.scopes.getOrDefault(scopeId, new ScopePrefs());
        scopePrefs.setTextColor(UtilsColor.toHex(color));
    }

    public Color getTextColor(String scopeId) {
        return Color.decode(this.scopes.getOrDefault(scopeId, new ScopePrefs()).getTextColor());
    }

    public String getLabel(String scopeId) {
        return this.scopes.getOrDefault(scopeId, new ScopePrefs()).getLabel();
    }

    public void setLabel(String scopeId, String label) {
        ScopePrefs scopePrefs = this.scopes.getOrDefault(scopeId, new ScopePrefs());
        scopePrefs.setLabel(label);
    }

    public int getFontSize(String scopeId) {
        String fontSize = this.scopes.getOrDefault(scopeId, new ScopePrefs()).getFontSize();
        return fontSize != null ? Integer.parseInt(fontSize) : 1;
    }

    public void setFontSize(String scopeId, int fontSize) {
        ScopePrefs scopePrefs = this.scopes.getOrDefault(scopeId, new ScopePrefs());
        scopePrefs.setFontSize(fontSize == -1 ? null : Integer.toString(fontSize));
        this.scopes.put(scopeId, scopePrefs);
    }

    public String getFontName(String scopeId) {
        String fontName = this.scopes.getOrDefault(scopeId, new ScopePrefs()).getFontName();
        return fontName == null ? "" : fontName;
    }

    public Font getFont(String scopeId) {
        String fontName = this.scopes.getOrDefault(scopeId, new ScopePrefs()).getFontName();
        return UtilsFont.getFontByName(fontName);
    }

    public void setFontName(String scopeId, String font) {
        ScopePrefs scopePrefs = this.scopes.getOrDefault(scopeId, new ScopePrefs());
        scopePrefs.setFontName(font.isEmpty() ? null : font);
    }
}
