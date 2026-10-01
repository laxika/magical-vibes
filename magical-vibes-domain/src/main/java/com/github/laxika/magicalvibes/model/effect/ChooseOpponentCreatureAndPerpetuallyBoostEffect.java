package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/**
 * Chooses an opponent-controlled creature at resolution and perpetually modifies its power and
 * toughness. The choice is deliberately non-targeting and can optionally be narrowed by a
 * permanent predicate.
 */
public record ChooseOpponentCreatureAndPerpetuallyBoostEffect(
        int powerBoost, int toughnessBoost, PermanentPredicate filter) implements CardEffect {

    public ChooseOpponentCreatureAndPerpetuallyBoostEffect(int powerBoost, int toughnessBoost) {
        this(powerBoost, toughnessBoost, null);
    }
}
