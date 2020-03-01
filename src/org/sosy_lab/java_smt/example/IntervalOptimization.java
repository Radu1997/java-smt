package org.sosy_lab.java_smt.example;

import com.google.common.collect.ImmutableList;
import org.sosy_lab.common.ShutdownManager;
import org.sosy_lab.common.configuration.Configuration;
import org.sosy_lab.common.configuration.InvalidConfigurationException;
import org.sosy_lab.common.log.BasicLogManager;
import org.sosy_lab.common.log.LogManager;
import org.sosy_lab.java_smt.SolverContextFactory;
import org.sosy_lab.java_smt.api.*;

import java.util.List;

public class IntervalOptimization {

    public static void main(String[] args) throws InvalidConfigurationException, InterruptedException, SolverException {
        Configuration config = Configuration.fromCmdLineArguments(args);
        LogManager logger = BasicLogManager.create(config);
        ShutdownManager shutdown = ShutdownManager.create();

        SolverContext context = SolverContextFactory.createSolverContext(
                config, logger, shutdown.getNotifier(), SolverContextFactory.Solvers.SMTINTERPOL);
        HoudiniApp houdini = new HoudiniApp(context);

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
        List<BooleanFormula> lemmas =
                ImmutableList.of(imgr.greaterThan(a, b), imgr.lessThan(a, b));

        List<BooleanFormula> result = houdini.houdini(lemmas, constraint);
    }
    }

