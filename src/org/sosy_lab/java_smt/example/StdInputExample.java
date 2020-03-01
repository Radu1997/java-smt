package org.sosy_lab.java_smt.example;

import org.sosy_lab.java_smt.api.ProverEnvironment;
import org.sosy_lab.java_smt.api.SolverContext;
import org.sosy_lab.java_smt.api.BooleanFormula;
import java.util.Scanner;

public class StdInputExample{

    public String readInput() {
        Scanner scan = new Scanner(System.in, Charset.defaultCharset().name());
        return s.nextLine();
    }

    public static void main(String[] args) {
        Configuration config = Configuration.fromCmdLineArguments(args);
        LogManager logger = BasicLogManager.create(config);
        ShutdownManager shutdown = ShutdownManager.create();

        SolverContext context = SolverContextFactory.createSolverContext(
                config, logger, shutdown.getNotifier(), SolverContextFactory.Solvers.SMTINTERPOL);
        ProverEnvironment prover = context.newProverEnvironment(ProverOptions.GENERATE_MODELS));
        prover.close();

    }
}