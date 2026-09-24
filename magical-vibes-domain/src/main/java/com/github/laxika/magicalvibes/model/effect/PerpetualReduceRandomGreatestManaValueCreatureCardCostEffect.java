package com.github.laxika.magicalvibes.model.effect;

/**
 * Selects a random greatest-mana-value creature card in the controller's hand and perpetually
 * reduces that card's generic cast cost by {@code amount}.
 */
public record PerpetualReduceRandomGreatestManaValueCreatureCardCostEffect(int amount)
        implements CardEffect {
}
