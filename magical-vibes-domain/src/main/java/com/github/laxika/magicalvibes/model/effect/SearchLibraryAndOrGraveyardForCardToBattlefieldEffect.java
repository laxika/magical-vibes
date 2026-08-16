package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.CardPredicate;

/**
 * Searches the controller's graveyard and/or library for one card matching a predicate and puts
 * it onto the battlefield. A matching graveyard card is used before the library branch; the
 * library branch is interactive and shuffles when the search is completed.
 */
public record SearchLibraryAndOrGraveyardForCardToBattlefieldEffect(CardPredicate filter)
        implements CardEffect {
}
