package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;

import java.util.Set;

/**
 * Causes the source permanent to become a copy of the supplied creature card until end of turn.
 *
 * <p>{@code additionalKeywords} are "except it has [keyword]" exceptions: they become part of the
 * copy's copiable values (CR 707.9b), so a further copy of the permanent has them too.
 */
public record BecomeCopyOfCardUntilEndOfTurnEffect(
        Card card,
        Set<CardType> additionalTypesOverride,
        Set<CardSubtype> additionalSubtypesOverride,
        Set<Keyword> additionalKeywords
) implements CardEffect {

    public BecomeCopyOfCardUntilEndOfTurnEffect {
        additionalTypesOverride = additionalTypesOverride == null
                ? Set.of() : Set.copyOf(additionalTypesOverride);
        additionalSubtypesOverride = additionalSubtypesOverride == null
                ? Set.of() : Set.copyOf(additionalSubtypesOverride);
        additionalKeywords = additionalKeywords == null
                ? Set.of() : Set.copyOf(additionalKeywords);
    }

    public BecomeCopyOfCardUntilEndOfTurnEffect(Card card, Set<CardType> additionalTypesOverride,
                                                Set<CardSubtype> additionalSubtypesOverride) {
        this(card, additionalTypesOverride, additionalSubtypesOverride, Set.of());
    }

    public BecomeCopyOfCardUntilEndOfTurnEffect(Card card) {
        this(card, Set.of(), Set.of(), Set.of());
    }
}
