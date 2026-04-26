/*
 * SonarQube Unisys C Plugin
 * Copyright (C) 2010-2025 SonarSource Sàrl
 * mailto:info AT sonarsource DOT com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the Sonar Source-Available License Version 1, as published by SonarSource SA.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the Sonar Source-Available License for more details.
 *
 * You should have received a copy of the Sonar Source-Available License
 * along with this program; if not, see https://sonarsource.com/license/ssal/
 */
package org.sonar.c.checks;

import com.sonar.sslr.api.AstNode;
import com.sonar.sslr.api.AstNodeType;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.Nullable;
import org.sonar.c.CCheck;
import org.sonar.c.CGrammar;
import org.sonar.check.Rule;

@Rule(key = "S122")
public class OneStatementPerLineCheck extends CCheck {

  private final Map<Integer, Integer> statementsPerLine = new HashMap<>();

  @Override
  public List<AstNodeType> subscribedTo() {
    return Arrays.asList(
        CGrammar.EXPRESSION_STATEMENT,
        CGrammar.CONTROL_STATEMENT,
        CGrammar.ITERATION_STATEMENT,
        CGrammar.JUMP_STATEMENT,
        CGrammar.DECLARATION);
  }

  @Override
  public void visitFile(@Nullable AstNode astNode) {
    statementsPerLine.clear();
  }

  @Override
  public void leaveFile(@Nullable AstNode astNode) {
    for (Map.Entry<Integer, Integer> entry : statementsPerLine.entrySet()) {
      if (entry.getValue() > 1) {
        addIssueAtLine(
            MessageFormat.format(
                "At most one statement is allowed per line, but {0} statements were found on this line.",
                entry.getValue()),
            entry.getKey());
      }
    }
    statementsPerLine.clear();
  }

  @Override
  public void visitNode(AstNode node) {
    if (!isTopLevel(node)) {
      return;
    }
    if (isEmptyBodyIteration(node)) {
      return;
    }
    statementsPerLine.merge(node.getTokenLine(), 1, Integer::sum);
  }

  private boolean isTopLevel(AstNode node) {
    int line = node.getTokenLine();
    AstNode parent = node.getParent();
    while (parent != null) {
      if (parent.getTokenLine() == line
          && parent.is(
              CGrammar.EXPRESSION_STATEMENT,
              CGrammar.CONTROL_STATEMENT,
              CGrammar.ITERATION_STATEMENT,
              CGrammar.JUMP_STATEMENT,
              CGrammar.DECLARATION)) {
        return false;
      }
      parent = parent.getParent();
    }
    return true;
  }

  private boolean isEmptyBodyIteration(AstNode node) {
    if (!node.is(CGrammar.EXPRESSION_STATEMENT)) {
      return false;
    }
    if (node.hasDirectChildren(CGrammar.EXPRESSION)) {
      return false;
    }
    AstNode parent = node.getParent();
    if (parent == null) {
      return false;
    }
    if (parent.is(CGrammar.ITERATION_STATEMENT)) {
      return true;
    }
    AstNode grandParent = parent.getParent();
    return grandParent != null && grandParent.is(CGrammar.ITERATION_STATEMENT);
  }
}
