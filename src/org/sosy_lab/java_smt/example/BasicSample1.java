/*
 *  JavaSMT is an API wrapper for a collection of SMT solvers.
 *  This file is part of JavaSMT.
 *
 *  Copyright (C) 2007-2019  Dirk Beyer
 *  All rights reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.sosy_lab.java_smt.example;

import org.sosy_lab.common.ShutdownNotifier;
import org.sosy_lab.common.configuration.Configuration;
import org.sosy_lab.common.configuration.InvalidConfigurationException;
import org.sosy_lab.common.log.BasicLogManager;
import org.sosy_lab.common.log.LogManager;
import org.sosy_lab.java_smt.SolverContextFactory;
import org.sosy_lab.java_smt.SolverContextFactory.Solvers;
import org.sosy_lab.java_smt.api.BooleanFormula;
import org.sosy_lab.java_smt.api.BooleanFormulaManager;
import org.sosy_lab.java_smt.api.IntegerFormulaManager;
import org.sosy_lab.java_smt.api.NumeralFormula.IntegerFormula;
import org.sosy_lab.java_smt.api.NumeralFormula.RationalFormula;
import org.sosy_lab.java_smt.api.ProverEnvironment;
import org.sosy_lab.java_smt.api.RationalFormulaManager;
import org.sosy_lab.java_smt.api.SolverContext;
import org.sosy_lab.java_smt.api.SolverContext.ProverOptions;
import org.sosy_lab.java_smt.api.SolverException;

public class BasicSample1 {
  public static void main(String[] args)
      throws InvalidConfigurationException, SolverException, InterruptedException {

    // Setup and Congigurations
    Configuration config = Configuration.fromCmdLineArguments(args);
    LogManager logger = BasicLogManager.create(config);

    // deal with cleanup and shutdowns
    // ShutdownManager shutdown = ShutdownManager.create();
    ShutdownNotifier shutdownNotifier = ShutdownNotifier.createDummy();

    // We can do this too but ...
    // SolverContext context = SolverContextFactory.createSolverContext(
    // config, logger, shutdown.getNotifier(), Solvers.SMTINTERPOL);

    Solvers solver = Solvers.SMTINTERPOL;

    // Instantiate JavaSMT with SMTInterpol as backend (for dependencies cf. documentation)
    try (SolverContext context =
        SolverContextFactory.createSolverContext(config, logger, shutdownNotifier, solver)) {

      /* BOOLEAN THEORY */
      // create the manager
      BooleanFormulaManager booleFormularMgr =
          context.getFormulaManager().getBooleanFormulaManager();

      // create atoms
      BooleanFormula xL = booleFormularMgr.makeVariable("xL");
      BooleanFormula xH = booleFormularMgr.makeVariable("xH");
      BooleanFormula yL = booleFormularMgr.makeVariable("yL");
      BooleanFormula yH = booleFormularMgr.makeVariable("yH");

      // create formular
      BooleanFormula lowXOR = booleFormularMgr.xor(xL, yL);
      BooleanFormula highXOR = booleFormularMgr.xor(xH, yH);
      BooleanFormula two_bit_adder = booleFormularMgr.and(lowXOR, highXOR); // Formula to solve

      /* LRA (Integer) THEORY */
      // create the manager
      IntegerFormulaManager intFormularMgr = context.getFormulaManager().getIntegerFormulaManager();

      // create atoms
      IntegerFormula x = intFormularMgr.makeVariable("x");
      IntegerFormula y = intFormularMgr.makeVariable("y");

      // create formula

      IntegerFormula two_x = intFormularMgr.multiply(x, intFormularMgr.makeNumber(2));
      IntegerFormula three_x = intFormularMgr.multiply(x, intFormularMgr.makeNumber(3));
      IntegerFormula two_y = intFormularMgr.multiply(y, intFormularMgr.makeNumber(2));

      // 3*x + y = 11
      BooleanFormula eqA1 =
          intFormularMgr.equal(intFormularMgr.add(three_x, y), intFormularMgr.makeNumber(11));
      // 2*x + y = 8
      BooleanFormula eqA2 =
          intFormularMgr.equal(intFormularMgr.add(two_x, y), intFormularMgr.makeNumber(8));

      BooleanFormula intTheorySampleA = booleFormularMgr.and(eqA1, eqA2); // Formula to solve (3,2)

      // x>2
      BooleanFormula eqB1 = intFormularMgr.greaterThan(x, intFormularMgr.makeNumber(2));
      // y<10
      BooleanFormula eqB2 = intFormularMgr.lessThan(y, intFormularMgr.makeNumber(10));
      // x+2*y == 7 (how do I differentiate)
      BooleanFormula eqB3 =
          intFormularMgr.equal(intFormularMgr.add(x, two_y), intFormularMgr.makeNumber(7));

      BooleanFormula intTheorySampleB = booleFormularMgr.and(eqB1, eqB2, eqB3); // Formula to solve
                                                                                // (0,7)

      /* LRA (Rational) THEORY */
      // create the manager
      RationalFormulaManager rationalFormularMgr =
          context.getFormulaManager().getRationalFormulaManager();

      // create atoms
      RationalFormula a = rationalFormularMgr.makeVariable("a");
      RationalFormula b = rationalFormularMgr.makeVariable("b");
      RationalFormula c = rationalFormularMgr.makeVariable("c");

      // create formula

      RationalFormula a_square = rationalFormularMgr.multiply(x, x);
      RationalFormula b_square = rationalFormularMgr.multiply(y, y);
      // a^2 + b^2 < 1
      BooleanFormula eqR1 =
          rationalFormularMgr.lessThan(
              rationalFormularMgr.add(a_square, b_square),
              rationalFormularMgr.makeNumber(1));
      // x*y > 0.1
      BooleanFormula eqR2 =
          rationalFormularMgr
              .greaterThan(rationalFormularMgr.multiply(a, b), rationalFormularMgr.makeNumber(0.1));

      BooleanFormula ratTheorySample1 = booleFormularMgr.and(eqB1, eqB2); // Formula to solve (1/8,
                                                                          // 7/8)

      // x*y > 1
      BooleanFormula eqR3 =
          rationalFormularMgr
              .greaterThan(rationalFormularMgr.multiply(a, b), rationalFormularMgr.makeNumber(1));

      BooleanFormula ratTheorySample2 = booleFormularMgr.and(eqB1, eqB2); // Formula to solve
                                                                          // (UNSAT)

      boolean isUnsat;

      // Solve formulae, get model, and print variable assignment
      try (ProverEnvironment prover = context.newProverEnvironment(ProverOptions.GENERATE_MODELS)) {

        prover.addConstraint(two_bit_adder);
        isUnsat = prover.isUnsat();
        assert !isUnsat;
        // try (Model model = prover.getModel()) {
          System.out.printf("SAT : 2-bit Adder ");
        // }

        prover.addConstraint(intTheorySampleA);
        isUnsat = prover.isUnsat();
        assert !isUnsat;
        // try (Model model = prover.getModel()) {
          // System.out.printf("SAT with a = %s, b = %s", model.evaluate(a), model.evaluate(b));
          System.out.printf("SAT : ");
        // }

        prover.addConstraint(intTheorySampleB);
        isUnsat = prover.isUnsat();
        assert !isUnsat;
        // try (Model model = prover.getModel()) {
          System.out.printf("SAT : ");
        // }

        prover.addConstraint(ratTheorySample1);
        isUnsat = prover.isUnsat();
        assert !isUnsat;
        // try (Model model = prover.getModel()) {
          System.out.printf("SAT : ");
        // }

        prover.addConstraint(ratTheorySample2);
        isUnsat = prover.isUnsat();
        assert isUnsat;
        // try (Model model = prover.getModel()) {
          System.out.printf("UNSAT : ");
        // }

      }
    }
  }

}
