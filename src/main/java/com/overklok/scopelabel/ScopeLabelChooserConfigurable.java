package com.overklok.scopelabel;

import com.intellij.ide.IdeBundle;
import com.intellij.ide.projectView.ProjectView;
import com.intellij.ide.util.scopeChooser.ScopeConfigurable;
import com.intellij.lang.LangBundle;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.fileEditor.ex.FileEditorManagerEx;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.options.SearchableConfigurable;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectBundle;
import com.intellij.openapi.ui.MasterDetailsComponent;
import com.intellij.openapi.ui.MasterDetailsStateService;
import com.intellij.openapi.ui.NamedConfigurable;
import com.intellij.openapi.util.Comparing;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.packageDependencies.DependencyValidationManager;
import com.intellij.psi.search.scope.impl.CustomScopesAggregator;
import com.intellij.psi.search.scope.packageSet.NamedScope;
import com.intellij.psi.search.scope.packageSet.NamedScopeManager;
import com.intellij.psi.search.scope.packageSet.NamedScopesHolder;
import com.intellij.ui.TreeSpeedSearch;
import com.intellij.util.ui.tree.TreeUtil;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.TreePath;
import java.util.*;

public class ScopeLabelChooserConfigurable extends MasterDetailsComponent implements SearchableConfigurable {
    @NonNls public static final String SCOPE_CHOOSER_CONFIGURABLE_UI_KEY = "ScopeLabelChooserConfigurable.UI";
    private final NamedScopesHolder myLocalScopesManager;
    private final NamedScopesHolder mySharedScopesManager;

    private final Project myProject;

    public ScopeLabelChooserConfigurable(final Project project) {
        super(new com.intellij.ide.util.scopeChooser.ScopeChooserConfigurable.ScopeChooserConfigurableState());
        myLocalScopesManager = NamedScopeManager.getInstance(project);
        mySharedScopesManager = DependencyValidationManager.getInstance(project);
        myProject = project;

        initTree();
    }

    @Override
    protected String getComponentStateKey() {
        return SCOPE_CHOOSER_CONFIGURABLE_UI_KEY;
    }

    @Override
    protected MasterDetailsStateService getStateService() {
        return MasterDetailsStateService.getInstance(myProject);
    }

    @Override
    protected DefaultActionGroup createToolbarActionGroup() { return null; }

    @Override
    public void reset() {
        myRoot.removeAllChildren();
        loadScopes(mySharedScopesManager);
        loadScopes(myLocalScopesManager);

        loadComponentState();

        final List<String> order = getScopesState().myOrder;
        TreeUtil.sortRecursively(myRoot, (o1, o2) -> {
            final int idx1 = order.indexOf(o1.getDisplayName());
            final int idx2 = order.indexOf(o2.getDisplayName());
            return idx1 - idx2;
        });

        if (getScopesState().myOrder.size() != myRoot.getChildCount()) {
            loadStateOrder();
        }

        super.reset();
    }


    @Override
    public void apply() throws ConfigurationException {
        checkForEmptyAndDuplicatedNames(ProjectBundle.message("rename.message.prefix.scope"),
                ProjectBundle.message("rename.scope.title"), ScopeConfigurable.class);
        checkForPredefinedNames();
        super.apply();

        loadStateOrder();

        refreshProject();
    }

    private void refreshProject() {
        if (myProject.isDefault()) return;
        FileEditorManagerEx fileEditorManager = FileEditorManagerEx.getInstanceEx(myProject);
        for (VirtualFile openVirtualFile : fileEditorManager.getOpenFiles()) {
            fileEditorManager.updateFilePresentation(openVirtualFile);
        }

        ProjectView.getInstance(myProject).refresh();
    }

    private void checkForPredefinedNames() throws ConfigurationException {
        final Set<String> predefinedScopes = new HashSet<>();
        for (NamedScope scope : CustomScopesAggregator.getAllCustomScopes(myProject)) {
            predefinedScopes.add(scope.getScopeId());
        }
        for (int i = 0; i < myRoot.getChildCount(); i++) {
            final MyNode node = (MyNode)myRoot.getChildAt(i);
            final NamedConfigurable scopeConfigurable = node.getConfigurable();
            final String name = scopeConfigurable.getDisplayName();
            if (predefinedScopes.contains(name)) {
                selectNodeInTree(node);
                throw new ConfigurationException(LangBundle.message("dialog.message.scope.name.equals.to.predefined.one"), ProjectBundle.message("rename.scope.title"));
            }
        }
    }

    public com.intellij.ide.util.scopeChooser.ScopeChooserConfigurable.ScopeChooserConfigurableState getScopesState() {
        return (com.intellij.ide.util.scopeChooser.ScopeChooserConfigurable.ScopeChooserConfigurableState)myState;
    }

    @Override
    public boolean isModified() {
        final List<String> order = getScopesState().myOrder;
        if (myRoot.getChildCount() != order.size()) return true;
        for (int i = 0; i < myRoot.getChildCount(); i++) {
            final MyNode node = (MyNode)myRoot.getChildAt(i);
            final ScopeLabelConfigurable scopeLabelConfigurable = (ScopeLabelConfigurable)node.getConfigurable();

            if (order.size() <= i) return true;
            final String name = order.get(i);
            if (!Comparing.strEqual(name, scopeLabelConfigurable.getScopeId())) return true;
            if (scopeLabelConfigurable.isInitialized()) {
                final NamedScopesHolder holder = scopeLabelConfigurable.getHolder();
                final NamedScope scope = holder.getScope(name);
                if (scope == null) return true;
                if (scopeLabelConfigurable.isModified()) return true;
            }
        }
        return false;
    }

    private void loadStateOrder() {
        final List<String> order = getScopesState().myOrder;
        order.clear();
        for (int i = 0; i < myRoot.getChildCount(); i++) {
            order.add(((MyNode)myRoot.getChildAt(i)).getDisplayName());
        }
    }

    private void loadScopes(final NamedScopesHolder holder) {
        final NamedScope[] scopes = holder.getScopes();
        for (NamedScope scope : scopes) {
            if (isPredefinedScope(scope)) continue;
            myRoot.add(new MyNode(new ScopeLabelConfigurable(scope, myProject)));
        }
    }

    private boolean isPredefinedScope(final NamedScope scope) {
        return getPredefinedScopes(myProject).contains(scope);
    }

    private static Collection<NamedScope> getPredefinedScopes(Project project) {
        final Collection<NamedScope> result = new ArrayList<>();
        result.addAll(NamedScopeManager.getInstance(project).getPredefinedScopes());
        result.addAll(DependencyValidationManager.getInstance(project).getPredefinedScopes());
        return result;
    }

    @Override
    protected void initTree() {
        myTree.getSelectionModel().addTreeSelectionListener(new TreeSelectionListener() {
            @Override
            public void valueChanged(TreeSelectionEvent e) {
                final TreePath path = e.getOldLeadSelectionPath();
                if (path != null) {
                    final MyNode node = (MyNode)path.getLastPathComponent();
                    final NamedConfigurable namedConfigurable = node.getConfigurable();
//                    if (namedConfigurable instanceof ScopeLabelConfigurable) {
//                        ((ScopeLabelConfigurable)namedConfigurable).cancelCurrentProgress();
//                    }
                }
            }
        });
        super.initTree();
        myTree.setShowsRootHandles(false);
        new TreeSpeedSearch(myTree, treePath -> ((MyNode)treePath.getLastPathComponent()).getDisplayName(), true);

        myTree.getEmptyText().setText(IdeBundle.message("scopes.no.scoped"));
    }

    @Override
    protected boolean wasObjectStored(Object editableObject) {
        if (editableObject instanceof NamedScope) {
            NamedScope scope = (NamedScope)editableObject;
            final String scopeId = scope.getScopeId();
            return myLocalScopesManager.getScope(scopeId) != null || mySharedScopesManager.getScope(scopeId) != null;
        }
        return false;
    }

    @Override
    public String getDisplayName() {
        return IdeBundle.message("scopes.display.name");
    }

    @Override
    protected void updateSelection(@Nullable final NamedConfigurable configurable) {
        if (configurable != null) {
            NamedScope scope = (NamedScope) configurable.getEditableObject();

            super.updateSelection(configurable);
        } else {
            super.updateSelection(null);
        }

        if (configurable instanceof ScopeConfigurable) {
            ((ScopeConfigurable)configurable).restoreCanceledProgress();
        }
    }

    @Override
    protected
    @Nullable
    String getEmptySelectionString() {
        return IdeBundle.message("scope.chooser.select.scope.text");
    }

    @Override
    @NotNull
    @NonNls
    public String getId() {
        return getHelpTopic();
    }
}
