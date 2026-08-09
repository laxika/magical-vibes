package com.github.laxika.magicalvibes.model;

/**
 * An alternate-casting-cost component that makes each opponent gain a fixed amount of life.
 */
public record EachOpponentGainsLifeCastingCost(int amount) implements CastingCost {

    public EachOpponentGainsLifeCastingCost {
        if (amount < 0) {
            throw new IllegalArgumentException("amount must be >= 0");
        }
    }
}
