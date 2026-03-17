package com.github.laxika.magicalvibes.model.effect;

/**
 * Composite effect: puts a slime counter on the source permanent, then creates a green Ooze
 * creature token whose P/T equals the number of slime counters on the source.
 * Used by Gutter Grime.
 */
public record PutSlimeCounterAndCreateOozeTokenEffect() implements CardEffect {

    @Override
    public boolean isSelfTargeting() { return true; }
}
