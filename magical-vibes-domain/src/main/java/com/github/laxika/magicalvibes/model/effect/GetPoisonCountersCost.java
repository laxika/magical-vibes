package com.github.laxika.magicalvibes.model.effect;

/**
 * "Get {@code count} poison counters" as a cost ("Ward—Get five poison counters"). A player who can't get poison
 * counters can't pay it (CR 118.3).
 *
 * @param count the poison counters the payer gets
 */
public record GetPoisonCountersCost(int count) implements CostEffect {

    public GetPoisonCountersCost {
        if (count <= 0) {
            throw new IllegalArgumentException("A poison cost gives at least one counter: " + count);
        }
    }
}
