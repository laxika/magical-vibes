package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.CardPredicate;

import java.util.Objects;

/**
 * Lets the controller discard any number of matching cards, then pushes a reflexive effect when
 * one or more cards were discarded. The discard count is available to the reflexive effect as the
 * stack entry event value.
 */
public record DiscardAnyNumberThenEffect(
        CardPredicate cardFilter,
        CardEffect thenEffect,
        String cardDescription
) implements CardEffect {

    public DiscardAnyNumberThenEffect {
        Objects.requireNonNull(cardFilter, "cardFilter");
        Objects.requireNonNull(thenEffect, "thenEffect");
        Objects.requireNonNull(cardDescription, "cardDescription");
    }
}
