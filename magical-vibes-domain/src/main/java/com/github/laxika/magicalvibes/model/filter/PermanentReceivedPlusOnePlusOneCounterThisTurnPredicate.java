package com.github.laxika.magicalvibes.model.filter;

public record PermanentReceivedPlusOnePlusOneCounterThisTurnPredicate(boolean byController) implements PermanentPredicate {

    public PermanentReceivedPlusOnePlusOneCounterThisTurnPredicate() {
        this(false);
    }
}
