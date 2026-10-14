package com.github.laxika.magicalvibes.model.condition;

/** At least one opponent of the controller has a life total at or below half the starting life total. */
public record AnOpponentLifeAtOrBelowHalfStartingLife() implements Condition {

    @Override
    public String conditionName() {
        return "an opponent at or below half the starting life total";
    }

    @Override
    public String conditionNotMetReason() {
        return "no opponent has life at or below half the starting life total";
    }
}
