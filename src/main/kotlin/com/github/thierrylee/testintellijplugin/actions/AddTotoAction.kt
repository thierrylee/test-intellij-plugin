package com.github.thierrylee.testintellijplugin.actions

import com.intellij.notification.NotificationGroupManager
import com.intellij.notification.NotificationType
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.LangDataKeys
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.psi.PsiClass
import com.intellij.psi.PsiJavaFile
import org.jetbrains.kotlin.psi.KtFile
import org.jetbrains.kotlin.psi.psiUtil.startOffset

class AddTotoAction: AnAction() {

    override fun actionPerformed(action: AnActionEvent) {
        val classes = findClasses(action)
        for (psiClass in classes) {
            generateClassDoc(action, psiClass)
        }

        val classNames = classes.joinToString(", ") { it.name ?: "" }
        showNotification(action, "Class documentation added for class $classNames")
    }

    private fun findClasses(action: AnActionEvent): Iterable<PsiClass> {
        val ktFile = action.getRequiredData(LangDataKeys.PSI_FILE) as? KtFile
        if (ktFile != null) {
            return ktFile.classes.asIterable()
        }

        val javaFile = action.getRequiredData(LangDataKeys.PSI_FILE) as? PsiJavaFile
        if (javaFile != null) {
            return javaFile.classes.asIterable()
        }

        return emptyList()
    }

    private fun generateClassDoc(action: AnActionEvent, psiClass: PsiClass) {
        WriteCommandAction.runWriteCommandAction(action.project) {
            val document = action.getRequiredData(LangDataKeys.EDITOR).document
            document.insertString(getClassStartOffset(psiClass), generateClassDoc(psiClass))
        }
    }

    private fun generateClassDoc(psiClass: PsiClass): String {
        return "/** Methods :\n * ${findMethods(psiClass).joinToString("\n * ")}\n */\n"
    }

    private fun findMethods(psiClass: PsiClass): List<String> {
        return psiClass.methods.map { it.name }
    }

    private fun getClassStartOffset(psiClass: PsiClass): Int {
        return psiClass.startOffset
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