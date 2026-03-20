package com.github.laxika.magicalvibes.model.effect;

/**
 * Deals damage to each opponent equal to the number of +1/+1 counters on the source permanent.
 * Used by cards like Hallar, the Firefletcher.
 */
public record DealDamageToEachOpponentEqualToPlusOnePlusOneCountersOnSourceEffect() implements CardEffect {

    @Override
    public boolean isSelfTargeting() { return true; }
}
