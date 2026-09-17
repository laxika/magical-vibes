package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.CardPredicate;

/**
 * Exiles cards from the top of a library until a matching card is found, then offers that card
 * to be cast without paying its mana cost. The other exiled cards are put on the bottom of the
 * library in a random order; the matching card remains exiled if the offer is declined.
 */
public record ExileUntilCardPredicateMayCastWithoutPayingManaEffect(CardPredicate predicate)
        implements CardEffect {
}
