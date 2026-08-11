package com.github.laxika.magicalvibes.model.effect;

/**
 * Target player draws cards equal to the number of cards in their hand, then discards that many
 * cards of their choice. The count is read before the draw.
 */
public record TargetPlayerDrawsCardsEqualToHandThenDiscardsThatManyEffect() implements CardEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetPredicates.player());
    }
}
