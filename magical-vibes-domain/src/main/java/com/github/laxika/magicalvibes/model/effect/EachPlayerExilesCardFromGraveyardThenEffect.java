package com.github.laxika.magicalvibes.model.effect;

import java.util.Objects;

/**
 * Each player exiles one card from their graveyard. If one or more nonland cards are exiled,
 * the supplied effect is put onto the stack as a reflexive ability with the nonland count as its
 * event value.
 */
public record EachPlayerExilesCardFromGraveyardThenEffect(CardEffect thenEffect) implements CardEffect {

    public EachPlayerExilesCardFromGraveyardThenEffect {
        Objects.requireNonNull(thenEffect, "thenEffect");
    }
}
