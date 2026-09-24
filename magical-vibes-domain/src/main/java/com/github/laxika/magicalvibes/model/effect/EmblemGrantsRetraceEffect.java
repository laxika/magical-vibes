package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Retrace;

import java.util.Optional;
import java.util.Set;

/**
 * Static effect stored in an emblem that grants retrace to matching cards in the controller's
 * graveyard. The spell uses its normal mana cost and returns to the graveyard after resolving.
 */
public record EmblemGrantsRetraceEffect(Set<CardType> cardTypes) implements EmblemGrantsCastingOptionEffect {

    @Override
    public Optional<Retrace> castingOption(Card card) {
        return Optional.of(new Retrace());
    }
}
