/*
 * Copyright 2000-2016 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.intellij.codeInsight.daemon.impl.quickfix;

import com.intellij.codeInsight.daemon.QuickFixBundle;
import com.intellij.codeInsight.intention.IntentionAction;
import com.intellij.ide.scratch.ScratchFileService;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import com.intellij.util.IncorrectOperationException;
import com.siyeh.ig.psiutils.CommentTracker;
import org.jetbrains.annotations.NotNull;

public class InsertNewFix implements IntentionAction {
  private final PsiMethodCallExpression myMethodCall;
  private final PsiClass myClass;

  public InsertNewFix(@NotNull PsiMethodCallExpression methodCall, @NotNull PsiClass aClass) {
    myMethodCall = methodCall;
    myClass = aClass;
  }

  @Override
  @NotNull
  public String getText() {
    return QuickFixBundle.message("insert.new.fix");
  }

  @Override
  @NotNull
  public String getFamilyName() {
    return getText();
  }

  @Override
  public boolean isAvailable(@NotNull Project project, Editor editor, PsiFile file) {
    return myMethodCall.isValid() && ScratchFileService.isInProjectOrScratch(myMethodCall) && !(myMethodCall.getNextSibling() instanceof PsiErrorElement);
  }

  @NotNull
  @Override
  public PsiElement getElementToMakeWritable(@NotNull PsiFile file) {
    return myMethodCall;
  }

  @Override
  public void invoke(@NotNull Project project, Editor editor, PsiFile file) throws IncorrectOperationException {
    PsiElementFactory factory = JavaPsiFacade.getElementFactory(myMethodCall.getProject());
    PsiNewExpression newExpression = (PsiNewExpression)factory.createExpressionFromText("new X()",null);

    CommentTracker tracker = new CommentTracker();
    PsiJavaCodeReferenceElement classReference = newExpression.getClassReference();
    assert classReference != null;
    classReference.replace(factory.createClassReferenceElement(myClass));
    PsiExpressionList argumentList = newExpression.getArgumentList();
    assert argumentList != null;
    argumentList.replace(tracker.markUnchanged(myMethodCall.getArgumentList()));
    tracker.replaceAndRestoreComments(myMethodCall, newExpression);
  }

  @Override
  public boolean startInWriteAction() {
    return true;
  }
}