package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;

import java.util.Objects;
import java.util.Set;
import java.util.function.Supplier;

/**
 * Puts a fresh card onto the battlefield under the ability's controller. {@code conjure} is false
 * when the card was drafted from a spellbook rather than conjured, so "whenever you conjure"
 * abilities don't trigger.
 */
public record ConjureCardOntoBattlefieldEffect(
        Supplier<? extends Card> cardFactory,
        Set<CardType> enterTappedTypes,
        boolean conjure
) implements CardEffect {

    public ConjureCardOntoBattlefieldEffect {
        Objects.requireNonNull(cardFactory, "Card factory cannot be null");
        enterTappedTypes = Set.copyOf(Objects.requireNonNull(enterTappedTypes,
                "Enter-tapped types cannot be null"));
    }
}
