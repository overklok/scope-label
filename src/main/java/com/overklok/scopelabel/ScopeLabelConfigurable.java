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
//        return new NamedScope(myScope.getScopeId(), myIcon, myPanel.getCurrentScope());
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
            preferencesPanel.setTextColor(projectPreferences.getTextColor());
            preferencesPanel.setBackgroundColor(projectPreferences.getBackgroundColor());
            preferencesPanel.setFontSize(projectPreferences.getFontSize(scopeId));
            preferencesPanel.setFontName(projectPreferences.getFontName());
            preferencesPanel.setLabel(projectPreferences.getLabel());
        }
        return preferencesPanel.getRootPanel();
    }

    public boolean isModified() {
        String scopeId = myScope.getScopeId();

        System.out.println("ism " + scopeId);

        return
                !UtilsColor.isEqual(projectPreferences.getBackgroundColor(), preferencesPanel.getBackgroundColor())
                        || !UtilsColor.isEqual(projectPreferences.getTextColor(), preferencesPanel.getTextColor())
                        || projectPreferences.getFontSize(scopeId) != preferencesPanel.getFontSize()
                        || !projectPreferences.getLabel().equals(preferencesPanel.getLabel())
                        || !projectPreferences.getFontName().equals(preferencesPanel.getFontName())
                        || applicationPreferences.getFontSize() != preferencesPanel.getGlobalFontSize()
                        || !applicationPreferences.getFontName().equals(preferencesPanel.getGlobalFontName());
    }

    public void apply() {
        System.out.println("apply");

        if (null != preferencesPanel) {
            String scopeId = myScope.getScopeId();

            System.out.println(scopeId);

            projectPreferences.setTextColor(preferencesPanel.getTextColor());
            projectPreferences.setBackgroundColor(preferencesPanel.getBackgroundColor());
            projectPreferences.setFontSize(scopeId, preferencesPanel.getFontSize());
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
