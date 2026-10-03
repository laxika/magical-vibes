package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardType;

import java.util.List;

/**
 * For each listed card type, optionally puts a matching permanent card from the controller's hand
 * onto the battlefield. The no-argument form derives the types from opponents' permanents when it
 * resolves.
 */
public record PutPermanentCardsOfTypesFromHandEffect(List<CardType> cardTypes,
                                                     boolean resolveTypesFromOpponents)
        implements CardEffect {

    public PutPermanentCardsOfTypesFromHandEffect() {
        this(List.of(), true);
    }

    public PutPermanentCardsOfTypesFromHandEffect(List<CardType> cardTypes) {
        this(cardTypes, false);
    }

    public PutPermanentCardsOfTypesFromHandEffect {
        cardTypes = List.copyOf(cardTypes);
    }
}
