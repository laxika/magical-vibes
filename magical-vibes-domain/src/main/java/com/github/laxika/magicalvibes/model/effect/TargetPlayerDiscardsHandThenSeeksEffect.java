package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.CardPredicate;

/** Target player discards their hand, then seeks one matching card for each card discarded. */
public record TargetPlayerDiscardsHandThenSeeksEffect(CardPredicate seekFilter) implements CardEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetPredicates.player());
    }
}
