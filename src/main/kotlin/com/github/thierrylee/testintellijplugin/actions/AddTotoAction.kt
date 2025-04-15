package com.github.thierrylee.testintellijplugin.actions

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.LangDataKeys

class AddTotoAction: AnAction() {

    override fun actionPerformed(action: AnActionEvent) {
        val fileContent = action.getRequiredData(LangDataKeys.EDITOR).document.text

        println("Methods :\n${findMethods(fileContent).joinToString("\n")}")
    }

    private fun findMethods(text: String): List<String> {
        return text.split("\n")
            .filter { it.contains("fun") }
            .map { it.trim() }
            .map { it.substringAfter("fun").substringBefore("(").trim() }
            .filter { it.isNotEmpty() }

    }

}