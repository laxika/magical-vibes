package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;

import java.util.Set;

/**
 * Causes the source permanent to become a copy of the supplied creature card until end of turn.
 */
public record BecomeCopyOfCardUntilEndOfTurnEffect(
        Card card,
        Set<CardType> additionalTypes,
        Set<CardSubtype> additionalSubtypes
) implements CardEffect {

    public BecomeCopyOfCardUntilEndOfTurnEffect {
        additionalTypes = additionalTypes == null ? Set.of() : Set.copyOf(additionalTypes);
        additionalSubtypes = additionalSubtypes == null ? Set.of() : Set.copyOf(additionalSubtypes);
    }

    public BecomeCopyOfCardUntilEndOfTurnEffect(Card card) {
        this(card, Set.of(), Set.of());
    }
}
