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
import org.sonar.c.CPunctuator;
import org.sonar.check.Rule;

import com.sonar.sslr.api.AstNode;
import com.sonar.sslr.api.AstNodeType;
import com.sonar.sslr.api.Token;

@Rule(key = "M23_162")
public class SignedBitFieldLengthCheck extends CCheck {

    @Override
    public List<AstNodeType> subscribedTo() {
        return Collections.singletonList(CGrammar.STRUCT_DECLARATOR);
    }

    @Override
    public void visitNode(AstNode node) {
        if (node.hasDirectChildren(CPunctuator.COLON)) {
            
            AstNode declarator = node.getFirstChild(CGrammar.DECLARATOR);
            if (declarator != null && declarator.hasDescendant(CGrammar.IDENTIFIER)) {
                if (isSignedType(node)) {
                    if (getBitFieldWidth(node) == 1) {
                        addIssue("A named signed bit-field shall not have a length of one bit.", node);
                    }
                }
            }
        }
    }

    private boolean isSignedType(AstNode node) {
        AstNode structDecl = node.getFirstAncestor(CGrammar.STRUCT_DECLARATION);
        if (structDecl == null) return false;

        AstNode typeList = structDecl.getFirstChild(CGrammar.TYPE_SPECIFIER_LIST);
        if (typeList != null) {
            List<Token> tokens = typeList.getTokens();
            boolean hasSigned = false;
            boolean hasUnsigned = false;
            boolean hasInt = false;

            for (Token t : tokens) {
                String val = t.getValue();
                if ("signed".equals(val)) hasSigned = true;
                if ("unsigned".equals(val)) hasUnsigned = true;
                if ("int".equals(val)) hasInt = true;
            }

            return hasSigned || (hasInt && !hasUnsigned);
        }
        return false;
    }

    private int getBitFieldWidth(AstNode node) {
        AstNode constantNode = node.getFirstDescendant(CGrammar.I_CONSTANT);
        if (constantNode != null) {
            try {
                return Integer.parseInt(constantNode.getTokenValue());
            } catch (NumberFormatException e) {
                return -1;
            }
        }
        return -1;
    }
}
