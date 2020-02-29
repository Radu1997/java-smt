package org.sosy_lab.java_smt.example;

import org.sosy_lab.common.ShutdownManager;
import org.sosy_lab.common.configuration.Configuration;
import org.sosy_lab.common.configuration.InvalidConfigurationException;
import org.sosy_lab.common.log.BasicLogManager;
import org.sosy_lab.common.log.LogManager;
import org.sosy_lab.java_smt.SolverContextFactory;
import org.sosy_lab.java_smt.api.*;

public class IntervalOptimization {

    public static void main(String[] args) throws InvalidConfigurationException, InterruptedException, SolverException {
        Configuration config = Configuration.fromCmdLineArguments(args);
        LogManager logger = BasicLogManager.create(config);
        ShutdownManager shutdown = ShutdownManager.create();

        SolverContext context = SolverContextFactory.createSolverContext(
                config, logger, shutdown.getNotifier(), SolverContextFactory.Solvers.SMTINTERPOL);

        FormulaManager fmgr = context.getFormulaManager();
        BooleanFormulaManager bmgr = fmgr.getBooleanFormulaManager();
        IntegerFormulaManager imgr = fmgr.getIntegerFormulaManager();

        NumeralFormula.IntegerFormula a = imgr.makeVariable("a"),
                b = imgr.makeVariable("b"),
                c = imgr.makeVariable("c");
        BooleanFormula constraint = bmgr.or(
                imgr.equal(
                        imgr.add(a, b), c
                ),
                imgr.equal(
                        imgr.add(a, c), imgr.multiply(imgr.makeNumber(2), b)
                )
        );

        try (ProverEnvironment prover = context.newProverEnvironment(SolverContext.ProverOptions.GENERATE_MODELS)) {
            prover.addConstraint(constraint);
            boolean isUnsat = prover.isUnsat();
        }
    }
    }

