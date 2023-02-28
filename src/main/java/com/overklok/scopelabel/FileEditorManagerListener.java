package com.overklok.scopelabel;

import com.intellij.openapi.fileEditor.FileEditorManagerEvent;
import org.jetbrains.annotations.NotNull;

public class FileEditorManagerListener implements com.intellij.openapi.fileEditor.FileEditorManagerListener {
    @Override
    public void selectionChanged(@NotNull FileEditorManagerEvent event) {
        System.out.println("selch");
        System.out.println(event.getNewFile().getPath());
    }
}
