package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.CardPredicate;

/**
 * Reveals cards from the top of the controller's library until a matching card is revealed. The
 * controller may put that card onto the battlefield; otherwise it goes into their hand, and the
 * other revealed cards are put on the bottom of the library in a random order.
 */
public record RevealUntilCardPredicateMayPutOntoBattlefieldElseToHandEffect(
        CardPredicate predicate
) implements CardEffect {
}
