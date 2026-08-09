package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.ManaColor;

/**
 * Until end of turn, a nonbasic land tapped for mana produces {@code color} instead of any other
 * type. The amount produced is unchanged; only the type is replaced.
 */
public record NonbasicLandsProduceFixedManaColorUntilEndOfTurnEffect(ManaColor color)
        implements CardEffect {
}
