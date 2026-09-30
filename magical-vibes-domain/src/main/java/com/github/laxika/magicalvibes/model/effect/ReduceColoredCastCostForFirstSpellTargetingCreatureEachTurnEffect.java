package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.ManaCost;

/** Reduces the colored mana cost of the first creature-targeting spell cast during each turn. */
public record ReduceColoredCastCostForFirstSpellTargetingCreatureEachTurnEffect(
        ManaCost reduction
) implements CardEffect {
}
