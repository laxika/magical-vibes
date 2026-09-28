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
        Set<CardType> additionalTypesOverride,
        Set<CardSubtype> additionalSubtypesOverride
) implements CardEffect {

    public BecomeCopyOfCardUntilEndOfTurnEffect {
        additionalTypesOverride = additionalTypesOverride == null
                ? Set.of() : Set.copyOf(additionalTypesOverride);
        additionalSubtypesOverride = additionalSubtypesOverride == null
                ? Set.of() : Set.copyOf(additionalSubtypesOverride);
    }

    public BecomeCopyOfCardUntilEndOfTurnEffect(Card card) {
        this(card, Set.of(), Set.of());
    }
}
