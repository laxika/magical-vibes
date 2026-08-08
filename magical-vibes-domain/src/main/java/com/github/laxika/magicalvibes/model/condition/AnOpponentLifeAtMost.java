package com.github.laxika.magicalvibes.model.condition;

/** At least one opponent of the controller has a life total at or below the threshold. */
public record AnOpponentLifeAtMost(int threshold) implements Condition {

    @Override
    public String conditionName() {
        return "an opponent at or below " + threshold + " life";
    }

    @Override
    public String conditionNotMetReason() {
        return "no opponent has " + threshold + " or less life";
    }
}
