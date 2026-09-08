package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardType;

import java.util.Set;

/**
 * The controller discards two cards unless they discard one card matching one of the specified
 * card types instead.
 */
public record DiscardTwoUnlessCardTypeEffect(Set<CardType> cardTypes) implements CardEffect {

    public DiscardTwoUnlessCardTypeEffect {
        cardTypes = Set.copyOf(cardTypes);
    }
}
