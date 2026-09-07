package com.github.laxika.magicalvibes.model.effect;

/**
 * Returns the target permanent to its owner's hand, then its controller may sacrifice a land. If
 * that player does, they may copy this spell and choose new targets for the copy.
 */
public record ReturnTargetPermanentToHandThenMayCopyEffect() implements RemovalEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetPredicates.permanent());
    }

    @Override
    public RemovalKind removalKind() {
        return RemovalKind.BOUNCE;
    }
}
