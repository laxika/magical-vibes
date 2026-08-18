package com.github.laxika.magicalvibes.model.condition;

/** At least {@code minimum} creatures died this turn, regardless of controller. */
public record CreatureDeathsThisTurnAtLeast(int minimum) implements Condition {

    @Override
    public String conditionName() {
        return minimum == 1
                ? "a creature died this turn"
                : minimum + " or more creatures died this turn";
    }

    @Override
    public String conditionNotMetReason() {
        return minimum == 1
                ? "no creature died this turn"
                : "fewer than " + minimum + " creatures died this turn";
    }
}
