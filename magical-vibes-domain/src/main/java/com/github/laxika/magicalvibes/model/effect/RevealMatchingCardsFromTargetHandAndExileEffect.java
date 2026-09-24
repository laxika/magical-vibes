package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.CardPredicate;

/** Reveals matching cards in a target player's hand, then lets the controller exile one. */
public record RevealMatchingCardsFromTargetHandAndExileEffect(
        CardPredicate filter, CardEffect thenEffect) implements CardEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetPredicates.player());
    }
}
