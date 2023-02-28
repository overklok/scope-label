package com.overklok.scopelabel;

import javax.swing.*;

import com.intellij.openapi.ui.NamedConfigurable;
import com.intellij.openapi.util.NlsContexts;
import com.intellij.openapi.util.NlsSafe;
import com.intellij.psi.search.scope.packageSet.NamedScope;
import com.intellij.psi.search.scope.packageSet.NamedScopeManager;
import com.intellij.psi.search.scope.packageSet.NamedScopesHolder;
import com.overklok.scopelabel.preferences.ApplicationPreferences;
import com.overklok.scopelabel.preferences.ProjectPreferences;
import com.overklok.scopelabel.resources.ui.PluginConfiguration;

import com.overklok.scopelabel.utils.UtilsColor;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import com.intellij.openapi.project.Project;

public class ScopeLabelConfigurable extends NamedConfigurable<NamedScope> {

    private PluginConfiguration preferencesPanel;

    private NamedScope myScope;
    private Icon myIcon;
    private final Project project;
    private final ProjectPreferences projectPreferences;
    private final ApplicationPreferences applicationPreferences;

    public ScopeLabelConfigurable(final NamedScope scope, final Project project) {
        this.myScope = scope;
        this.project = project;
        this.projectPreferences = ProjectPreferences.getInstance(project);
        this.applicationPreferences = ApplicationPreferences.getInstance();

        myIcon = getHolder().getIcon();
    }

    public String getDisplayName() {
        return myScope.getPresentableName();
    }

    @Nullable
    @NonNls
    public String getHelpTopic() {
        return null;
    }

    @NotNull NamedScopesHolder getHolder() {
        return NamedScopeManager.getInstance(project);
    }

    @Override
    public void setDisplayName(@NlsSafe String name) {

    }

    @Override
    public NamedScope getEditableObject() {
        return myScope;
//        return projectPreferences.getScopePrefs().get(myScope.getScopeId());
//        return new NamedScope(myScope.getScopeId(), myIcon, myScope.getValue());
    }

    public String getScopeId() {
        return myScope.getScopeId();
    }

    @Override
    public @NlsContexts.DetailedDescription String getBannerSlogan() {
        return null;
    }

    @Override
    public JComponent createOptionsPanel() {
        if (null == preferencesPanel) {
            String scopeId = myScope.getScopeId();

            preferencesPanel = new PluginConfiguration();
            preferencesPanel.setGlobalFontSize(applicationPreferences.getFontSize());
            preferencesPanel.setGlobalFontName(applicationPreferences.getFontName());
            preferencesPanel.setTextColor(projectPreferences.getTextColor(scopeId));
            preferencesPanel.setBackgroundColor(projectPreferences.getBackgroundColor(scopeId));
            preferencesPanel.setFontSize(projectPreferences.getFontSize(scopeId));
            preferencesPanel.setFontName(projectPreferences.getFontName(scopeId));
            preferencesPanel.setLabel(projectPreferences.getLabel(scopeId));
        }
        return preferencesPanel.getRootPanel();
    }

    public boolean isInitialized() {
        return null != preferencesPanel;
    }

    public boolean isModified() {
        String scopeId = myScope.getScopeId();

        return
                !UtilsColor.isEqual(projectPreferences.getBackgroundColor(scopeId), preferencesPanel.getBackgroundColor())
                        || !UtilsColor.isEqual(projectPreferences.getTextColor(scopeId), preferencesPanel.getTextColor())
                        || projectPreferences.getFontSize(scopeId) != preferencesPanel.getFontSize()
                        || !projectPreferences.getLabel(scopeId).equals(preferencesPanel.getLabel())
                        || !projectPreferences.getFontName(scopeId).equals(preferencesPanel.getFontName())
                        || applicationPreferences.getFontSize() != preferencesPanel.getGlobalFontSize()
                        || !applicationPreferences.getFontName().equals(preferencesPanel.getGlobalFontName());
    }

    public void apply() {
        if (null != preferencesPanel) {
            String scopeId = myScope.getScopeId();

            projectPreferences.setTextColor(scopeId, preferencesPanel.getTextColor());
            projectPreferences.setBackgroundColor(scopeId, preferencesPanel.getBackgroundColor());
            projectPreferences.setFontSize(scopeId, preferencesPanel.getFontSize());
            projectPreferences.setFontName(scopeId, preferencesPanel.getFontName());
            projectPreferences.setLabel(scopeId, preferencesPanel.getLabel());
            applicationPreferences.setFontSize(preferencesPanel.getGlobalFontSize());
            applicationPreferences.setFontName(preferencesPanel.getGlobalFontName());
            if (project != null) {
                ScopeLabelProjectComponent component = project.getComponent(ScopeLabelProjectComponent.class);
                if (component != null) {
                    component.onSettingsChanged();
                }
            }
        }
    }

    public void disposeUIResources() {
        preferencesPanel = null;
    }
}
