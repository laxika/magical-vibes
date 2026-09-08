package com.github.laxika.magicalvibes.model.effect;

/**
 * The target player reveals their hand and discards every card that shares a color with the
 * creature sacrificed as this spell's additional cost.
 */
public record DiscardAllCardsOfSacrificedCreatureColorsEffect() implements CardEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetPredicates.player());
    }
}
