package com.overklok.scopelabel;

import javax.swing.*;

import com.intellij.openapi.ui.NamedConfigurable;
import com.intellij.openapi.util.NlsContexts;
import com.intellij.openapi.util.NlsSafe;
import com.intellij.packageDependencies.DependencyValidationManager;
import com.intellij.psi.search.scope.packageSet.NamedScope;
import com.intellij.psi.search.scope.packageSet.NamedScopeManager;
import com.intellij.psi.search.scope.packageSet.NamedScopesHolder;
import com.overklok.scopelabel.preferences.ApplicationPreferences;
import com.overklok.scopelabel.preferences.ProjectPreferences;
import com.overklok.scopelabel.resources.ui.PluginConfiguration;

import com.overklok.scopelabel.utils.UtilsColor;
import com.intellij.openapi.options.Configurable;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import com.intellij.openapi.project.Project;

import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.Nullable;

public class ScopeLabelConfigurable extends NamedConfigurable<ScopeLabelConfigurable> {

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
        return "Project Label";
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
    public ScopeLabelConfigurable getEditableObject() {
        return null;
//        return new NamedScope(myScope.getScopeId(), myIcon, myPanel.getCurrentScope());
    }

    @Override
    public @NlsContexts.DetailedDescription String getBannerSlogan() {
        return null;
    }

    @Override
    public JComponent createOptionsPanel() {
        if (null == preferencesPanel) {
            preferencesPanel = new PluginConfiguration();
            preferencesPanel.setGlobalFontSize(applicationPreferences.getFontSize());
            preferencesPanel.setGlobalFontName(applicationPreferences.getFontName());
            preferencesPanel.setTextColor(projectPreferences.getTextColor());
            preferencesPanel.setBackgroundColor(projectPreferences.getBackgroundColor());
            preferencesPanel.setFontSize(projectPreferences.getFontSize());
            preferencesPanel.setFontName(projectPreferences.getFontName());
            preferencesPanel.setLabel(projectPreferences.getLabel());
        }
        return preferencesPanel.getRootPanel();
    }

    public boolean isModified() {
        return
                !UtilsColor.isEqual(projectPreferences.getBackgroundColor(), preferencesPanel.getBackgroundColor())
                        || !UtilsColor.isEqual(projectPreferences.getTextColor(), preferencesPanel.getTextColor())
                        || projectPreferences.getFontSize() != preferencesPanel.getFontSize()
                        || !projectPreferences.getLabel().equals(preferencesPanel.getLabel())
                        || !projectPreferences.getFontName().equals(preferencesPanel.getFontName())
                        || applicationPreferences.getFontSize() != preferencesPanel.getGlobalFontSize()
                        || !applicationPreferences.getFontName().equals(preferencesPanel.getGlobalFontName());
    }

    public void apply() {
        if (null != preferencesPanel) {
            projectPreferences.setTextColor(preferencesPanel.getTextColor());
            projectPreferences.setBackgroundColor(preferencesPanel.getBackgroundColor());
            projectPreferences.setFontSize(preferencesPanel.getFontSize());
            projectPreferences.setFontName(preferencesPanel.getFontName());
            projectPreferences.setLabel(preferencesPanel.getLabel());
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
