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

        val classDoc = "/** Methods :\n * ${findMethods(fileContent).joinToString("\n * ")}\n */\n"
        val classLineNumber = getClassLineNumber(fileContent)

        WriteCommandAction.runWriteCommandAction(action.project) {
            val document = action.getRequiredData(LangDataKeys.EDITOR).document
            val charOffset = document.getLineStartOffset(classLineNumber)
            document.insertString(charOffset, classDoc)
        }

        NotificationGroupManager.getInstance()
            .getNotificationGroup("ClassDoc")
            .createNotification(
                "Class documentation added for class ${findClass(fileContent)}",
                NotificationType.INFORMATION
            )
            .notify(action.project)
    }

    private fun findMethods(text: String): List<String> {
        return text.split("\n")
            .filter { it.contains("fun") }
            .map { it.trim() }
            .map { it.substringAfter("fun").substringBefore("(").trim() }
            .filter { it.isNotEmpty() }
    }

    private fun getClassLineNumber(text: String): Int {
        return text.split("\n")
            .indexOfFirst { it.contains("class ") }
    }

    private fun findClass(text: String): String {
        return text.split("\n")
            .firstOrNull { it.contains("class ") }
            ?.substringAfter("class ")
            ?.substringBefore("{")
            ?.trim()
            ?: ""
    }

}