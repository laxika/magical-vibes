package com.github.laxika.magicalvibes.model.effect;

/**
 * Returns the target permanent to its owner's hand, then reduces its controller's speed by one
 * when that controller is faster than every other player.
 */
public record ReturnTargetPermanentToHandAndReduceControllerSpeedEffect() implements RemovalEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetPredicates.permanent());
    }

    @Override
    public RemovalKind removalKind() {
        return RemovalKind.BOUNCE;
    }
}
