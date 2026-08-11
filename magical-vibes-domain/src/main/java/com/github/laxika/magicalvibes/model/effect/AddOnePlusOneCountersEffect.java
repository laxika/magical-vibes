package com.github.laxika.magicalvibes.model.effect;

/**
 * Hardened Scales: "If one or more +1/+1 counters would be put on a creature you control, that
 * many plus one +1/+1 counters are put on it instead."
 */
public record AddOnePlusOneCountersEffect() implements PlusOnePlusOneCountersReplacementEffect {

    @Override
    public int replace(int count) {
        return count > 0 ? count + 1 : count;
    }
}
