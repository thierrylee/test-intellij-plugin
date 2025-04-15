package com.github.thierrylee.testintellijplugin.actions

import com.intellij.notification.NotificationGroupManager
import com.intellij.notification.NotificationType
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.LangDataKeys
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.psi.JavaPsiFacade
import com.intellij.psi.PsiClass
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiJavaFile
import org.jetbrains.kotlin.idea.base.util.reformat
import org.jetbrains.kotlin.psi.KtFile
import org.jetbrains.kotlin.psi.KtPsiFactory

class AddTotoAction : AnAction() {

    override fun actionPerformed(action: AnActionEvent) {
        val (classes, psiRoot) = findClasses(action)
        if (psiRoot != null) {
            for (psiClass in classes) {
                generateClassDoc(action, psiRoot, psiClass)
            }

            val classNames = classes.joinToString(", ") { it.name ?: "" }
            showNotification(action, "Class documentation added for class $classNames")
        } else {
            showNotification(action, "Wrong file type :/")
        }
    }

    private fun findClasses(action: AnActionEvent): Pair<Iterable<PsiClass>, PsiElement?> {
        val ktFile = action.getRequiredData(LangDataKeys.PSI_FILE) as? KtFile
        if (ktFile != null) {
            return ktFile.classes.asIterable() to ktFile
        }

        val javaFile = action.getRequiredData(LangDataKeys.PSI_FILE) as? PsiJavaFile
        if (javaFile != null) {
            return javaFile.classes.asIterable() to javaFile
        }

        return emptyList<PsiClass>() to null
    }

    private fun generateClassDoc(action: AnActionEvent, psiRoot: PsiElement, psiClass: PsiClass) {
        val classDoc = generateClassDoc(psiClass)

        val psiClassDoc = when (psiRoot) {
            is PsiJavaFile -> {
                val psiFactory = JavaPsiFacade.getElementFactory(action.project!!)
                psiFactory.createDocCommentFromText(classDoc, psiClass)
            }

            is KtFile -> {
                val psiFactory = KtPsiFactory(action.project!!, false)
                psiFactory.createBlockCodeFragment(classDoc, psiRoot)
            }

            else -> null
        }

        psiClassDoc?.let {
            WriteCommandAction.runWriteCommandAction(action.project) {
                psiRoot.addBefore(psiClassDoc, psiRoot.firstChild)
                psiRoot.reformat()
            }
        }

    }

    private fun generateClassDoc(psiClass: PsiClass): String {
        return """
/** Methods :
 * ${findMethods(psiClass).joinToString("\n * ")}
 */
 """.trimIndent()
    }

    private fun findMethods(psiClass: PsiClass): List<String> {
        return psiClass.methods.map { it.name }
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