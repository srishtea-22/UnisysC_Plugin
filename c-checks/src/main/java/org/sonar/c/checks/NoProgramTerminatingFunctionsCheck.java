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

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.sonar.c.CCheck;
import org.sonar.c.CGrammar;
import org.sonar.c.CPunctuator;
import org.sonar.check.Rule;

import com.sonar.sslr.api.AstNode;
import com.sonar.sslr.api.AstNodeType;

@Rule(key = "M23_202")
public class NoProgramTerminatingFunctionsCheck extends CCheck {

    // _Exit and quick_exit excluded — grammar IDENTIFIER regex [a-zA-Z][a-zA-Z0-9]*+
    // does not support leading or embedded underscores
    private static final Set<String> TERMINATING_FUNCTIONS = Collections.unmodifiableSet(
        new HashSet<>(Arrays.asList(
            "exit",
            "abort"
        ))
    );

    @Override
    public List<AstNodeType> subscribedTo() {
        return Collections.singletonList(CGrammar.POSTFIX_EXPRESSION);
    }

    @Override
    public void visitNode(AstNode postfixExpr) {
        if (!postfixExpr.hasDirectChildren(CPunctuator.LPARENTHESIS)) {
            return;
        }

        AstNode identifier = getFirstIdentifier(postfixExpr);
        if (identifier == null) {
            return;
        }

        String functionName = identifier.getTokenValue();
        if (functionName == null) {
            return;
        }

        if (TERMINATING_FUNCTIONS.contains(functionName)) {
            addIssue(
                "Program-terminating functions shall not be used: '" + functionName + "'.",
                identifier
            );
        }
    }

    private AstNode getFirstIdentifier(AstNode node) {
        if (node.is(CGrammar.IDENTIFIER)) {
            return node;
        }
        for (AstNode child : node.getChildren()) {
            if (child.is(CPunctuator.LPARENTHESIS)) {
                break;
            }
            AstNode found = getFirstIdentifier(child);
            if (found != null) {
                return found;
            }
        }
        return null;
    }
}