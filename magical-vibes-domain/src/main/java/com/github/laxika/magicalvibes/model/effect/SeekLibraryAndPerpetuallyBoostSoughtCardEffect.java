package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.CardPredicate;

import java.util.Objects;

/** Seeks one matching card into hand and gives that exact card a perpetual power/toughness boost. */
public record SeekLibraryAndPerpetuallyBoostSoughtCardEffect(
        CardPredicate filter, int powerBoost, int toughnessBoost) implements CardEffect {

    public SeekLibraryAndPerpetuallyBoostSoughtCardEffect {
        Objects.requireNonNull(filter, "filter");
    }
}
