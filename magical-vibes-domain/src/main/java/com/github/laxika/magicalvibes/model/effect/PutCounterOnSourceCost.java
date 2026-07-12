package com.github.laxika.magicalvibes.model.effect;

/**
 * Activation cost: put {@code count} counters (described by {@code powerModifier}/{@code toughnessModifier},
 * e.g. -1/-1 or +1/+1) on the source permanent. Mirrors {@link PutCountersOnSourceEffect} but as a cost,
 * so it is paid immediately on activation. Used by Barrenton Medic ("Put a -1/-1 counter on this creature: ...").
 */
public record PutCounterOnSourceCost(int powerModifier, int toughnessModifier, int count) implements CostEffect {

    public PutCounterOnSourceCost() {
        this(-1, -1, 1);
    }
}
