package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.CardPredicate;

/**
 * Reveals cards until a matching card is found, then offers that card as an optional battlefield
 * entry. The found card and all other revealed cards are shuffled into the library after the
 * choice.
 */
public record RevealUntilCardPredicateMayPutOntoBattlefieldShuffleRestEffect(CardPredicate predicate)
        implements CardEffect {
}
