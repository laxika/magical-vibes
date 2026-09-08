package com.github.laxika.magicalvibes.model.effect;

/**
 * Target player discards any number of cards, then draws that many cards.
 */
public record TargetPlayerDiscardsAnyNumberThenDrawsThatManyEffect() implements CardEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetPredicates.player());
    }
}
