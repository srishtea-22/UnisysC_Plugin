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
import org.sonar.c.CKeyword;
import org.sonar.check.Rule;

import com.sonar.sslr.api.AstNode;
import com.sonar.sslr.api.AstNodeType;

@Rule(key = "S1301")
public class PreferIfOverSwitchCheck extends CCheck {

    private static final int MIN_CASES_FOR_SWITCH = 4;

    @Override
    public List<AstNodeType> subscribedTo() {
        return Collections.singletonList(CGrammar.CONTROL_STATEMENT);
    }

    @Override
    public void visitNode(AstNode controlStatement) {
        AstNode firstChild = controlStatement.getFirstChild();
        if (firstChild == null || !firstChild.is(CKeyword.SWITCH)) {
            return;
        }

        int labelCount = countCaseLabels(controlStatement);

        if (labelCount < MIN_CASES_FOR_SWITCH) {
            addIssue(
                "\"if\" statement should be preferred over this \"switch\""
                + " which has only " + labelCount + " case(s).",
                firstChild
            );
        }
    }

    private int countCaseLabels(AstNode controlStatement) {
        int count = 0;

        for (AstNode labeled : controlStatement.getDescendants(CGrammar.LABELED_STATEMENT)) {
            AstNode labelFirst = labeled.getFirstChild();
            if (labelFirst == null) {
                continue;
            }
            if (labelFirst.is(CKeyword.CASE) || labelFirst.is(CKeyword.DEFAULT)) {
                count++;
            }
        }

        return count;
    }
}