/*
 *  JavaSMT is an API wrapper for a collection of SMT solvers.
 *  This file is part of JavaSMT.
 *
 *  Copyright (C) 2007-2015  Dirk Beyer
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
package org.sosy_lab.java_smt.solvers.cvc4;

import edu.nyu.acsys.CVC4.Expr;
import edu.nyu.acsys.CVC4.ExprManager;
import edu.nyu.acsys.CVC4.Kind;
import edu.nyu.acsys.CVC4.Type;
import edu.nyu.acsys.CVC4.vectorExpr;
import java.util.Collection;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import org.sosy_lab.java_smt.api.BooleanFormula;
import org.sosy_lab.java_smt.basicimpl.AbstractBooleanFormulaManager;

public class CVC4BooleanFormulaManager
    extends AbstractBooleanFormulaManager<Expr, Type, ExprManager, Expr> {

  private final Expr cvc4True;
  private final Expr cvc4False;
  private final ExprManager exprManager;

  protected CVC4BooleanFormulaManager(CVC4FormulaCreator pCreator) {
    super(pCreator);
    exprManager = pCreator.getEnv();
    cvc4True = exprManager.mkConst(true);
    cvc4False = exprManager.mkConst(false);
  }

  @Override
  protected Expr makeVariableImpl(String pVar) {
    return formulaCreator.makeVariable(getFormulaCreator().getBoolType(), pVar);
  }

  @Override
  protected Expr makeBooleanImpl(boolean pValue) {
    return exprManager.mkConst(pValue);
  }

  @Override
  protected Expr not(Expr pParam1) {
    return exprManager.mkExpr(Kind.NOT, pParam1);
  }

  @Override
  protected Expr and(Expr pParam1, Expr pParam2) {
    if (isTrue(pParam1)) {
      return pParam2;
    } else if (isTrue(pParam2)) {
      return pParam1;
    } else if (isFalse(pParam1)) {
      return cvc4False;
    } else if (isFalse(pParam2)) {
      return cvc4False;
    } else if (pParam1 == pParam2) {
      return pParam1;
    }
    return exprManager.mkExpr(Kind.AND, pParam1, pParam2);
  }

  @Override
  protected Expr andImpl(Collection<Expr> pParams) {
    vectorExpr vExpr = new vectorExpr();
    for (Expr e : pParams) {
      if (isFalse(e)) {
        return cvc4False;
      }
      if (!isTrue(e)) {
        vExpr.add(e);
      }
    }
    if (vExpr.capacity() == 0) {
      return cvc4True;
    } else if (vExpr.capacity() == 1) {
      return vExpr.get(0);
    } else {
      return exprManager.mkExpr(Kind.AND, vExpr);
    }
  }

  @Override
  public Collector<BooleanFormula, ?, BooleanFormula> toConjunction() {
    return Collectors.collectingAndThen(Collectors.toList(), this::and);
  }

  @Override
  protected Expr or(Expr pParam1, Expr pParam2) {
    if (isTrue(pParam1)) {
      return cvc4True;
    } else if (isTrue(pParam2)) {
      return cvc4True;
    } else if (isFalse(pParam1)) {
      return pParam2;
    } else if (isFalse(pParam2)) {
      return pParam1;
    } else if (pParam1 == pParam2) {
      return pParam1;
    }
    return exprManager.mkExpr(Kind.OR, pParam1, pParam2);
  }

  @Override
  protected Expr orImpl(Collection<Expr> pParams) {
    vectorExpr vExpr = new vectorExpr();
    for (Expr e : pParams) {
      if (isTrue(e)) {
        return cvc4True;
      }
      if (!isFalse(e)) {
        vExpr.add(e);
      }
    }
    if (vExpr.capacity() == 0) {
      return cvc4False;
    } else if (vExpr.capacity() == 1) {
      return vExpr.get(0);
    } else {
      return exprManager.mkExpr(Kind.OR, vExpr);
    }
  }

  @Override
  public Collector<BooleanFormula, ?, BooleanFormula> toDisjunction() {
    return Collectors.collectingAndThen(Collectors.toList(), this::or);
  }

  @Override
  protected Expr xor(Expr pParam1, Expr pParam2) {
    return exprManager.mkExpr(Kind.XOR, pParam1, pParam2);
  }

  @Override
  protected Expr equivalence(Expr pBits1, Expr pBits2) {
    return exprManager.mkExpr(Kind.EQUAL, pBits1, pBits2);
  }

  @Override
  protected boolean isTrue(Expr pBits) {
    return pBits.isConst() && pBits.getConstBoolean();
  }

  @Override
  protected boolean isFalse(Expr pBits) {
    return pBits.isConst() && !pBits.getConstBoolean();
  }

  @Override
  protected Expr ifThenElse(Expr pCond, Expr pF1, Expr pF2) {
    return exprManager.mkExpr(Kind.ITE, pCond, pF1, pF2);
  }
}
