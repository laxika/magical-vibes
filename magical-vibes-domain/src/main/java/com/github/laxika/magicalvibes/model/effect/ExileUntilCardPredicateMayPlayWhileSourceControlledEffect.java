package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.CardPredicate;

/**
 * Exiles cards from the top of a library until a matching card is found. The matching card may
 * be played from exile while the source permanent remains under its controller's control; the
 * other exiled cards are put on the bottom of that library in a random order.
 */
public record ExileUntilCardPredicateMayPlayWhileSourceControlledEffect(CardPredicate predicate)
        implements CardEffect {
}
