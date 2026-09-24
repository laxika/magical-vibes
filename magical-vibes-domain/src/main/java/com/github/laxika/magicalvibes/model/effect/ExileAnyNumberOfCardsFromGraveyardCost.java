package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.CardPredicate;

/**
 * Optional additional cost that exiles any number of matching cards from the caster's graveyard
 * and reduces the spell's generic mana cost by a fixed amount for each card exiled. Exiled cards
 * are tracked with the spell so that the entering permanent can reference them later.
 *
 * @param predicate the cards that may be exiled
 * @param genericReductionPerCard the generic mana reduction for each card exiled
 */
public record ExileAnyNumberOfCardsFromGraveyardCost(CardPredicate predicate, int genericReductionPerCard)
        implements CostEffect {

    public ExileAnyNumberOfCardsFromGraveyardCost {
        if (genericReductionPerCard < 0) {
            throw new IllegalArgumentException("generic reduction must not be negative");
        }
    }
}
