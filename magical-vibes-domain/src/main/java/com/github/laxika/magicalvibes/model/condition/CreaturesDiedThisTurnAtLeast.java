package com.github.laxika.magicalvibes.model.condition;

/** At least the given number of creatures died this turn, regardless of controller. */
public record CreaturesDiedThisTurnAtLeast(int minimum) implements Condition {

    @Override
    public String conditionName() {
        return minimum + " or more creatures died this turn";
    }

    @Override
    public String conditionNotMetReason() {
        return "fewer than " + minimum + " creatures died this turn";
    }
}
