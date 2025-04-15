package com.github.thierrylee.testintellijplugin.actions

import com.intellij.notification.NotificationGroupManager
import com.intellij.notification.NotificationType
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.LangDataKeys
import com.intellij.openapi.command.WriteCommandAction

class AddTotoAction: AnAction() {

    override fun actionPerformed(action: AnActionEvent) {
        val fileContent = action.getRequiredData(LangDataKeys.EDITOR).document.text
        generateClassDoc(action)
        showNotification(action, "Class documentation added for class ${findClass(fileContent)}")
    }

    private fun generateClassDoc(action: AnActionEvent) {
        val fileContent = action.getRequiredData(LangDataKeys.EDITOR).document.text

        WriteCommandAction.runWriteCommandAction(action.project) {
            val document = action.getRequiredData(LangDataKeys.EDITOR).document
            val charOffset = document.getLineStartOffset(getClassLineNumber(fileContent))
            document.insertString(charOffset, generateClassDoc(fileContent))
        }
    }

    private fun generateClassDoc(fileContent: String): String {
        return "/** Methods :\n * ${findMethods(fileContent).joinToString("\n * ")}\n */\n"
    }

    private fun findMethods(fileContent: String): List<String> {
        return fileContent.split("\n")
            .filter { it.contains("fun") }
            .map { it.trim() }
            .map { it.substringAfter("fun").substringBefore("(").trim() }
            .filter { it.isNotEmpty() }
    }

    private fun getClassLineNumber(fileContent: String): Int {
        return fileContent.split("\n")
            .indexOfFirst { it.contains("class ") }
    }

    private fun findClass(fileContent: String): String {
        return fileContent.split("\n")
            .firstOrNull { it.contains("class ") }
            ?.substringAfter("class ")
            ?.substringBefore("{")
            ?.trim()
            ?: ""
    }

    private fun showNotification(action: AnActionEvent, content: String) {
        NotificationGroupManager.getInstance()
            .getNotificationGroup("ClassDoc")
            .createNotification(
                content,
                NotificationType.INFORMATION
            )
            .notify(action.project)
    }

}