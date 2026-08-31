package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.CardPredicate;

/**
 * The target player reveals a number of random cards from their hand, then discards every
 * revealed card matching {@code predicate}.
 *
 * @param count     maximum number of cards to reveal; the whole hand is revealed when it contains
 *                  fewer cards
 * @param predicate predicate identifying which revealed cards are discarded
 */
public record RevealRandomCardsFromTargetHandDiscardMatchingEffect(int count, CardPredicate predicate)
        implements CardEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetPredicates.player());
    }
}
