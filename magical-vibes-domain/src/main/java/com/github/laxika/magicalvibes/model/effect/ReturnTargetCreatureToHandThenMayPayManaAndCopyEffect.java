package com.github.laxika.magicalvibes.model.effect;

/**
 * Returns the target creature to its owner's hand, then its controller may pay the configured
 * mana cost. If they do, they may copy the resolving spell and choose new targets for the copy.
 */
public record ReturnTargetCreatureToHandThenMayPayManaAndCopyEffect(String manaCost)
        implements RemovalEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetPredicates.creature());
    }

    @Override
    public RemovalKind removalKind() {
        return RemovalKind.BOUNCE;
    }
}
