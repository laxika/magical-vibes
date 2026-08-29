package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.CardPredicate;

/**
 * Reveals cards from the top of the controller's library until a card matching the predicate is
 * revealed. That card is put into the controller's hand, and all other revealed cards are put
 * into the controller's graveyard. If the library is exhausted without a match, every revealed
 * card is put into the graveyard.
 */
public record RevealUntilCardPredicateToHandRestToGraveyardEffect(CardPredicate predicate)
        implements CardEffect {
}
