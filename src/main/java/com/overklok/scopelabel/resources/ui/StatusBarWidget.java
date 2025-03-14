package com.overklok.scopelabel.resources.ui;

import com.intellij.openapi.fileEditor.FileEditorManagerEvent;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.search.scope.packageSet.NamedScope;
import com.intellij.psi.search.scope.packageSet.NamedScopesHolder;
import com.intellij.psi.search.scope.packageSet.PackageSet;
import com.intellij.psi.search.scope.packageSet.PackageSetBase;
//import com.intellij.ui.tabs.FileColorConfiguration;
import com.overklok.scopelabel.FileEditorManagerListener;
import com.overklok.scopelabel.preferences.ApplicationPreferences;
import com.overklok.scopelabel.preferences.ProjectPreferences;
import com.overklok.scopelabel.preferences.ScopePrefs;
import com.overklok.scopelabel.utils.UtilsFont;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.options.ShowSettingsUtil;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.CustomStatusBarWidget;
import com.intellij.openapi.wm.StatusBar;
import com.intellij.ui.JBColor;
import com.intellij.ui.scale.JBUIScale;
import com.intellij.util.ui.UIUtil;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;
import javax.swing.*;
import java.awt.*;
import java.awt.font.FontRenderContext;
import java.awt.font.TextAttribute;
import java.awt.image.BufferedImage;
import java.util.Map;

public class StatusBarWidget extends JButton implements CustomStatusBarWidget {

    private final static Logger LOG = Logger.getInstance(StatusBarWidget.class);

    @NonNls
    public static final String WIDGET_ID = "ProjectLabelWidget";

    private static final int HORIZONTAL_PADDING = 18;
    private static final int VERTICAL_PADDING = 2;
    private static final int HEIGHT = 12;

    private Project project;

    private Dimension textDimension;
    private Image bufferedImage;

    private ProjectPreferences projectPreferences;
    private ApplicationPreferences applicationPreferences;

    private String label;
    private Color backgroundColor;
    private Color textColor;
    private Font font;
    private VirtualFile currentFile;

    private static RenderingHints HINTS;

    static {
        HINTS = new RenderingHints(RenderingHints.KEY_ALPHA_INTERPOLATION,
                RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
        HINTS.put(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);
        HINTS.put(RenderingHints.KEY_RENDERING,
                RenderingHints.VALUE_RENDER_QUALITY);
        HINTS.put(RenderingHints.KEY_TEXT_ANTIALIASING,
                RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        HINTS.put(RenderingHints.KEY_FRACTIONALMETRICS,
                RenderingHints.VALUE_FRACTIONALMETRICS_ON);
    }

    public StatusBarWidget(final Project project, ProjectPreferences projectPreferences, ApplicationPreferences applicationPreferences) {
        addActionListener(event -> {
            rebuildWidget();
            updateUI();
            ShowSettingsUtil.getInstance().showSettingsDialog(project, "Project Label");
        });

        this.project = project;
        this.projectPreferences = projectPreferences;
        this.applicationPreferences = applicationPreferences;

        this.currentFile = project.getProjectFile();

        setStateFromSettings();

        setOpaque(false);
        setFocusable(false);
        setBorder(StatusBarWidget.WidgetBorder.INSTANCE);
        repaint();
        updateUI();

        this.project.getMessageBus().connect(this).subscribe(FileEditorManagerListener.FILE_EDITOR_MANAGER, new FileEditorManagerListener() {
            @Override
            public void selectionChanged(@Nonnull FileEditorManagerEvent event) {
                currentFile = event.getNewFile();
                rebuildWidget();
            }
        });
    }

    @Nullable
    private String findScope(@NotNull final VirtualFile file) {
        Map<String, ScopePrefs> scopes = projectPreferences.getScopePrefs();

        if (scopes == null) {
            return null;
        }

        for (Map.Entry<String, ScopePrefs> entry : scopes.entrySet()) {
            String scopeId = entry.getKey();
            NamedScope scope = NamedScopesHolder.getScope(project, scopeId);
            if (scope != null) {
                NamedScopesHolder namedScopesHolder = NamedScopesHolder.getHolder(project, scopeId, null);
                PackageSet packageSet = scope.getValue();

                if (packageSet instanceof PackageSetBase && namedScopesHolder != null && ((PackageSetBase)packageSet).contains(file, project, namedScopesHolder)) {
                    return scopeId;
                }
            }
        }
        return null;
    }

    private void setStateFromSettings() {
        VirtualFile file = this.currentFile != null ? this.currentFile : project.getProjectFile();

        String scopeId = findScope(file);

        backgroundColor = new JBColor(projectPreferences.getBackgroundColor(scopeId), projectPreferences.getBackgroundColor(scopeId));
        textColor = new JBColor(projectPreferences.getTextColor(scopeId), projectPreferences.getTextColor(scopeId));
        label = projectPreferences.getLabel(scopeId).isEmpty() ? this.project.getName().toUpperCase() : projectPreferences.getLabel(scopeId);

        float projectPreferencesFontSize = projectPreferences.getFontSize(scopeId);
        float applicationPreferencesFontSize = applicationPreferences.getFontSize();
        float fontSize = JBUIScale.scaleFontSize(projectPreferencesFontSize == -1 ? applicationPreferencesFontSize : projectPreferencesFontSize);
        if (fontSize == 0) {
            fontSize = 8;
        }

        font = projectPreferences.getFontName(scopeId).isEmpty() ? applicationPreferences.getFont() : projectPreferences.getFont(scopeId);
        if (font == null) {
            font = UtilsFont.getFontByName("Dialog");
        }
        font = UtilsFont.setAttributes(font, TextAttribute.WEIGHT_ULTRABOLD, fontSize);
    }

    public void rebuildWidget() {
        try {
            setStateFromSettings();
            bufferedImage = null;
            textDimension = null;
            repaint();
        } catch (Throwable e) {
            LOG.error(e);
        }
    }

    private Dimension getTextDimensions() {
        if (textDimension == null) {
            FontRenderContext renderContext = new FontRenderContext(font.getTransform(), true, true);

            textDimension = new Dimension(
                    (int) (font.getStringBounds(label, renderContext).getWidth()),
                    (int) (font.getStringBounds(label, renderContext).getHeight())
            );
        }

        return textDimension;
    }

    @Override
    public void dispose() {
    }

    @Override
    public void install(@NotNull StatusBar statusBar) {
    }

    @Override
    @NotNull
    public String ID() {
        return WIDGET_ID;
    }

    @Override
    public void updateUI() {
        super.updateUI();
        bufferedImage = null;
    }

    @Override
    public JComponent getComponent() {
        return this;
    }

    @Override
    public void paintComponent(final Graphics graphics) {
        if (bufferedImage == null) {
            int labelWidth = getTextDimensions().width;

            Dimension size = getSize();
            final Dimension arcs = new Dimension(8, 8);

            // image
            bufferedImage = UIUtil.createImage(size.width, size.height, BufferedImage.TYPE_INT_ARGB);
            Graphics2D graphics2D = (Graphics2D) bufferedImage.getGraphics().create();

            graphics2D.setRenderingHints(HINTS);

            // background
            graphics2D.setColor(backgroundColor);
            graphics2D.fillRoundRect(0, 0, size.width, size.height, arcs.width, arcs.height);

            // label
            graphics2D.setColor(textColor);
            graphics2D.setFont(font);

            FontMetrics metrics = graphics.getFontMetrics(font);

            graphics2D.drawString(
                    label,
                    (size.width - labelWidth) / 2,
                    (size.height - metrics.getHeight()) / 2 + metrics.getAscent()
            );
            graphics2D.dispose();
        }

        UIUtil.drawImage(graphics, bufferedImage, 0, 0, null);
    }

    @Override
    public Dimension getPreferredSize() {
        int width = getTextDimensions().width + (HORIZONTAL_PADDING * 2);
        int textHeight = getTextDimensions().height;
        int height = textHeight > HEIGHT ? textHeight + (VERTICAL_PADDING * 2) : HEIGHT;
        return new Dimension(JBUIScale.scale(width), JBUIScale.scale(height));
    }

    @Override
    public Dimension getMinimumSize() {
        return getPreferredSize();
    }

    @Override
    public Dimension getMaximumSize() {
        return getPreferredSize();
    }
}
