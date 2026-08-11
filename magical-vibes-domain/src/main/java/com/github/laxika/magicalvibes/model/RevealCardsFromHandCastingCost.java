package com.github.laxika.magicalvibes.model;

import com.github.laxika.magicalvibes.model.filter.CardPredicate;

/**
 * Reveals a card from hand as part of an alternate casting cost without moving that card.
 */
public record RevealCardsFromHandCastingCost(CardPredicate predicate, String label) implements CastingCost {
}
