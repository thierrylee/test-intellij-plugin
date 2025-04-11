package com.github.thierrylee.testintellijplugin.actions

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent

class AddTotoAction: AnAction() {

    override fun actionPerformed(action: AnActionEvent) {
        print("Toto !!")
    }

}