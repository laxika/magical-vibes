package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.CardPredicate;

import java.util.Objects;

/** Mills each player, then puts one chosen matching milled card onto the battlefield. */
public record MillEachPlayerAndPutMilledCardOntoBattlefieldEffect(int count, CardPredicate filter)
        implements CardEffect {

    public MillEachPlayerAndPutMilledCardOntoBattlefieldEffect {
        if (count < 0) {
            throw new IllegalArgumentException("count cannot be negative");
        }
        Objects.requireNonNull(filter, "filter");
    }
}
