package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.CardPredicate;

/**
 * Reveals matching cards in a target player's hand, then lets the controller choose one, which stays in the hand or,
 * without {@code keepInHand}, is exiled. The step pauses at its end and records {@code StepKind.CHOOSE} or
 * {@code StepKind.EXILE}, so what happens to that card is a later step.
 */
public record RevealMatchingCardsFromTargetHandAndKeepEffect(CardPredicate filter, boolean keepInHand)
        implements CardEffect {

    public RevealMatchingCardsFromTargetHandAndKeepEffect(CardPredicate filter) {
        this(filter, true);
    }

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetPredicates.player());
    }
}
