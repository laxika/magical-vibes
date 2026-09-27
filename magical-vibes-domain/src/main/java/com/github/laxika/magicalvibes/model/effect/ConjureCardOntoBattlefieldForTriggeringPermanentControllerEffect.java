package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;

import java.util.Objects;
import java.util.Set;
import java.util.function.Supplier;

/** Conjures a fresh card onto the battlefield under the controller of the triggering permanent. */
public record ConjureCardOntoBattlefieldForTriggeringPermanentControllerEffect(
        Supplier<? extends Card> cardFactory,
        Set<CardType> enterTappedTypes
) implements CardEffect {

    public ConjureCardOntoBattlefieldForTriggeringPermanentControllerEffect {
        Objects.requireNonNull(cardFactory, "Card factory cannot be null");
        enterTappedTypes = Set.copyOf(Objects.requireNonNull(enterTappedTypes,
                "Enter-tapped types cannot be null"));
    }
}
