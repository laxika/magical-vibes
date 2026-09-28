/**
 * Chooses an opponent-controlled creature at resolution and perpetually modifies its power and
 * toughness. The choice is deliberately non-targeting.
 */
package com.github.laxika.magicalvibes.model.effect;

public record ChooseOpponentCreatureAndPerpetuallyBoostEffect(int powerBoost, int toughnessBoost)
        implements CardEffect {
}
