package com.github.laxika.magicalvibes.model.condition;

import com.github.laxika.magicalvibes.model.amount.CountScope;

/** At least {@code minimum} creatures died this turn in the selected player scope. */
public record CreatureDeathsThisTurnAtLeast(int minimum, CountScope scope) implements Condition {

    public CreatureDeathsThisTurnAtLeast(int minimum) {
        this(minimum, CountScope.ANY_PLAYER);
    }

    public CreatureDeathsThisTurnAtLeast {
        if (minimum < 1) {
            throw new IllegalArgumentException("minimum must be positive");
        }
        if (scope != CountScope.ANY_PLAYER && scope != CountScope.CONTROLLER) {
            throw new IllegalArgumentException("scope must be ANY_PLAYER or CONTROLLER");
        }
    }

    @Override
    public String conditionName() {
        if (scope == CountScope.CONTROLLER) {
            return minimum == 1
                    ? "a creature died under your control this turn"
                    : minimum + " or more creatures died under your control this turn";
        }
        return minimum == 1
                ? "a creature died this turn"
                : minimum + " or more creatures died this turn";
    }

    @Override
    public String conditionNotMetReason() {
        if (scope == CountScope.CONTROLLER) {
            return minimum == 1
                    ? "no creature died under your control this turn"
                    : "fewer than " + minimum + " creatures died under your control this turn";
        }
        return minimum == 1
                ? "no creature died this turn"
                : "fewer than " + minimum + " creatures died this turn";
    }
}
