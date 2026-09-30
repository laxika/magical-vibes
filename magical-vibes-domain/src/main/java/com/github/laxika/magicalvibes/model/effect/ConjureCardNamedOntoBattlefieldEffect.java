package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardType;

import java.util.Set;

/** Conjures a card from a known printing directly onto the controller's battlefield. */
public record ConjureCardNamedOntoBattlefieldEffect(
        String setCode,
        String collectorNumber,
        Set<CardType> enterTappedTypes
) implements CardEffect {

    public ConjureCardNamedOntoBattlefieldEffect(String setCode, String collectorNumber) {
        this(setCode, collectorNumber, Set.of());
    }

    public ConjureCardNamedOntoBattlefieldEffect {
        enterTappedTypes = Set.copyOf(enterTappedTypes);
    }
}
