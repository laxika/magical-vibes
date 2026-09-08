package com.github.laxika.magicalvibes.model.effect;

/**
 * Returns a targeted permanent to its owner's hand, exiling it first when it entered the
 * battlefield through unearth.
 */
public record ReturnTargetPermanentToHandOrExileIfUnearthedEffect() implements RemovalEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetPredicates.permanent());
    }

    @Override
    public RemovalKind removalKind() {
        return RemovalKind.BOUNCE;
    }
}
