package com.github.laxika.magicalvibes.model.effect;

/**
 * Put this permanent on the bottom of its owner's library as an activation cost.
 */
public record PutSelfOnBottomOfOwnersLibraryCost() implements CostEffect {

    @Override
    public boolean consumesSourcePermanent() {
        return true;
    }
}
