package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.CardPredicate;

/**
 * Punisher effect: the target player reveals their hand, then for each revealed card matching
 * {@code cardFilter} that player discards it unless they pay {@code lifeCost} life. One independent
 * decision per matching card, made by the target player; a card whose payment can't be afforded (too
 * little life, or life can't change) is discarded with no prompt. Used by Sirocco.
 *
 * @param cardFilter which revealed cards are at risk (Sirocco: blue instant cards)
 * @param lifeCost   life the target player may pay per matching card to keep it
 */
public record RevealHandDiscardMatchingCardsUnlessPaysLifeEffect(CardPredicate cardFilter, int lifeCost)
        implements CardEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetPredicates.player());
    }
}
