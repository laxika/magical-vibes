package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.CardPredicate;

/**
 * Optional additional cost that exiles any number of matching cards from the caster's hand and
 * reduces the spell's generic mana cost by a fixed amount for each card exiled.
 *
 * @param predicate the cards that may be exiled
 * @param genericReductionPerCard the generic mana reduction for each exiled card
 */
public record ExileAnyNumberOfCardsFromHandCost(CardPredicate predicate, int genericReductionPerCard)
        implements CostEffect {

    public ExileAnyNumberOfCardsFromHandCost {
        if (genericReductionPerCard < 0) {
            throw new IllegalArgumentException("generic reduction must not be negative");
        }
    }
}
