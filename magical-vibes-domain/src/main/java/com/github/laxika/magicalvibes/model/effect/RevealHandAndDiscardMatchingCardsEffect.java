package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.CardPredicate;

/**
 * The target player reveals their hand and discards every card matching {@code predicate}.
 */
public record RevealHandAndDiscardMatchingCardsEffect(CardPredicate predicate) implements CardEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetPredicates.player());
    }
}
