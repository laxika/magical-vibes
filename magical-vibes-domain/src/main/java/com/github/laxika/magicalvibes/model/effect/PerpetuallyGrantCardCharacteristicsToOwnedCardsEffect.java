package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;

import java.util.Set;

/** Perpetually grants card types and subtypes to matching cards owned by the effect controller. */
public record PerpetuallyGrantCardCharacteristicsToOwnedCardsEffect(
        CardPredicate filter,
        Set<CardType> cardTypes,
        Set<CardSubtype> subtypes
) implements CardEffect {

    public PerpetuallyGrantCardCharacteristicsToOwnedCardsEffect {
        cardTypes = Set.copyOf(cardTypes);
        subtypes = Set.copyOf(subtypes);
    }

    public PerpetuallyGrantCardCharacteristicsToOwnedCardsEffect(
            CardPredicate filter, CardType cardType, CardSubtype subtype) {
        this(filter, Set.of(cardType), Set.of(subtype));
    }
}
