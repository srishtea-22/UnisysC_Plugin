package org.sonar.c.checks;

import java.io.File;
import org.junit.jupiter.api.Test;
import org.sonar.c.checks.LocalVarShadowsFieldCheck;

public class MC23_150_CheckTest {

    private MC23_150_Check check = new MC23_150_Check();

    @Test
    public void test() {
        CVerifier.verify(new File("src/test/resources/checks/MC23_150.ccc_m"), check);
    }
}