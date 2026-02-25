package com.github.laxika.magicalvibes.model.effect;

/**
 * Cost that removes N charge counters from the source permanent.
 * Used by cards like Golem Foundry ("Remove three charge counters: ...").
 */
public record RemoveChargeCountersFromSourceCost(int count) implements CardEffect {
}
