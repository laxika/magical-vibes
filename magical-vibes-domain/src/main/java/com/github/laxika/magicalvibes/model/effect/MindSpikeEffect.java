package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.CardPredicate;

/** Reveals matching cards from a target player's hand and discards one, or draws if none match. */
public record MindSpikeEffect(CardPredicate filter) implements CardEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetPredicates.player());
    }
}
