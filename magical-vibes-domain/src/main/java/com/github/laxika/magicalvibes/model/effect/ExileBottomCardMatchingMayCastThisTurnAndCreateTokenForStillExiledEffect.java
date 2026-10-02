package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.CardPredicate;

import java.util.Objects;

/** Exiles the bottom matching card, grants a temporary cast permission, and tracks it for a delayed token. */
public record ExileBottomCardMatchingMayCastThisTurnAndCreateTokenForStillExiledEffect(
        CardPredicate filter,
        CreateTokenEffect tokenEffect
) implements CardEffect {

    public ExileBottomCardMatchingMayCastThisTurnAndCreateTokenForStillExiledEffect {
        Objects.requireNonNull(filter, "filter");
        Objects.requireNonNull(tokenEffect, "tokenEffect");
    }
}
