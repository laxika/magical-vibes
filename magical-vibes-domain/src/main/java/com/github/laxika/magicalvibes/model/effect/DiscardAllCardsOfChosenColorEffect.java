package com.github.laxika.magicalvibes.model.effect;

/**
 * The controller chooses a color at resolution, then the target player reveals their hand and
 * discards every card of that color. Used by Persecute.
 */
public record DiscardAllCardsOfChosenColorEffect() implements CardEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetCategory.PLAYER);
    }
}
