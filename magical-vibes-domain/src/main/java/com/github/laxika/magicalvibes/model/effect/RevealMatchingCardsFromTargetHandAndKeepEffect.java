package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.CardPredicate;

/** Reveals matching cards in a target player's hand, then lets the controller choose one. */
public record RevealMatchingCardsFromTargetHandAndKeepEffect(
        CardPredicate filter, CardEffect chosenCardThenEffect, boolean keepInHand) implements CardEffect {

    public RevealMatchingCardsFromTargetHandAndKeepEffect(
            CardPredicate filter, CardEffect chosenCardThenEffect) {
        this(filter, chosenCardThenEffect, true);
    }

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetPredicates.player());
    }
}
