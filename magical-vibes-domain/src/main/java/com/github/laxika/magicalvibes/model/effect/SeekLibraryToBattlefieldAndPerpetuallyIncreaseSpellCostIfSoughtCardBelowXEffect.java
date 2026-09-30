package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.CardPredicate;

import java.util.Objects;

/** Seeks one matching card onto the battlefield and conditionally taxes the resolving spell. */
public record SeekLibraryToBattlefieldAndPerpetuallyIncreaseSpellCostIfSoughtCardBelowXEffect(
        CardPredicate filter, int genericCastCostIncrease) implements CardEffect {

    public SeekLibraryToBattlefieldAndPerpetuallyIncreaseSpellCostIfSoughtCardBelowXEffect {
        Objects.requireNonNull(filter, "filter");
        if (genericCastCostIncrease <= 0) {
            throw new IllegalArgumentException("Generic cast-cost increase must be positive");
        }
    }
}
