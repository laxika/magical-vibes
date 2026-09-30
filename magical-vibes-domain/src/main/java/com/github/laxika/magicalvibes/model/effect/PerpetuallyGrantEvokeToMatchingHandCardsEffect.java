package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.CardPredicate;

import java.util.Objects;

/** Perpetually grants a fixed Evoke alternate cost to matching cards currently in hand. */
public record PerpetuallyGrantEvokeToMatchingHandCardsEffect(
        CardPredicate cardFilter, CardPredicate exiledCardFilter, String exiledCardLabel)
        implements CardEffect {

    public PerpetuallyGrantEvokeToMatchingHandCardsEffect {
        Objects.requireNonNull(cardFilter, "cardFilter");
        Objects.requireNonNull(exiledCardFilter, "exiledCardFilter");
        if (exiledCardLabel == null || exiledCardLabel.isBlank()) {
            throw new IllegalArgumentException("Exiled card label must not be blank");
        }
    }
}
