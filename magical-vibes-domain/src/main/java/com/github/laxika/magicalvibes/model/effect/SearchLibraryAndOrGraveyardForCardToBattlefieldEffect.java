package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.CardPredicate;

/**
 * Searches the controller's graveyard, optionally hand, and/or library for one card matching a
 * predicate and puts it onto the battlefield. A matching graveyard card is used before the hand
 * and library branches; the library branch is interactive and shuffles when the search is
 * completed.
 *
 * @param includeHand whether the controller's hand is also searched
 * @param attachToSource whether the found permanent is attached to the source permanent
 */
public record SearchLibraryAndOrGraveyardForCardToBattlefieldEffect(
        CardPredicate filter,
        boolean includeHand,
        boolean attachToSource
) implements CardEffect {

    public SearchLibraryAndOrGraveyardForCardToBattlefieldEffect(CardPredicate filter) {
        this(filter, false, false);
    }
}
