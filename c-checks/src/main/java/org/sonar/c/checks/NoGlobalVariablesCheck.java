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

import java.util.Collections;
import java.util.List;

import org.sonar.c.CCheck;
import org.sonar.c.CGrammar;
import org.sonar.check.Rule;

import com.sonar.sslr.api.AstNode;
import com.sonar.sslr.api.AstNodeType;

@Rule(key = "M23_388")
public class NoGlobalVariablesCheck extends CCheck {
    @Override
    public List<AstNodeType> subscribedTo() {
        return Collections.singletonList(CGrammar.DECLARATION);
    }
    @Override
    public void visitNode(AstNode node) {
        AstNode parent = node.getParent();

        // Global declarations have EXTERNAL_DEFINITION as their direct parent
        if (parent == null || !parent.is(CGrammar.EXTERNAL_DEFINITION)) {
            return;
        }

        // Skip typedef declarations
        AstNode declSpecs = node.getFirstChild(CGrammar.DECLARATION_SPECIFIERS);
        if (declSpecs != null) {
            for (AstNode child : declSpecs.getChildren()) {
                if (child.is(CGrammar.STORAGE_CLASS_SPECIFIER)
                        && "typedef".equalsIgnoreCase(child.getTokenValue())) {
                    return;
                }
            }
        }

        // Skip pure struct/union type declarations with no variable name
        if (!node.hasDirectChildren(CGrammar.INIT_DECLARATOR_LIST)) {
            return;
        }

        // Skip function prototypes: DIRECT_DECLARATOR contains a LPARENTHESIS child
        // e.g. void doWork(void); — looks like a declaration but is a function
        // prototype
        AstNode initDeclList = node.getFirstChild(CGrammar.INIT_DECLARATOR_LIST);
        if (initDeclList != null) {
            AstNode initDecl = initDeclList.getFirstChild(CGrammar.INIT_DECLARATOR);
            if (initDecl != null) {
                AstNode declarator = initDecl.getFirstChild(CGrammar.DECLARATOR);
                if (declarator != null) {
                    AstNode directDecl = declarator.getFirstChild(CGrammar.DIRECT_DECLARATOR);
                    if (directDecl != null) {
                        for (AstNode child : directDecl.getChildren()) {
                            if ("(".equals(child.getTokenValue())) {
                                return; // It's a function prototype, not a variable
                            }
                        }
                    }
                }
            }
        }

        addIssue("Global variables shall not be used.", node);
    }
}