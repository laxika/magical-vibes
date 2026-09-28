package com.github.laxika.magicalvibes.model.effect;

/**
 * Perpetually gives the controller's other creatures and creature cards in hand a power/toughness
 * bonus. The effect is deliberately scoped to those two zones, unlike an owned-card perpetual
 * modifier.
 */
public record PerpetuallyBoostOtherControlledCreaturesAndHandCardsEffect(
        int powerBoost, int toughnessBoost) implements CardEffect {
}
