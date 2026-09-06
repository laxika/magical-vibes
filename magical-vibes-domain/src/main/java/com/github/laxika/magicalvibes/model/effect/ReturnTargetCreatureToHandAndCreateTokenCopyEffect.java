package com.github.laxika.magicalvibes.model.effect;

/**
 * Returns the target creature to its owner's hand, then creates a token copy of that creature
 * under the effect controller. The handler keeps the creature's card available as last-known
 * information after the bounce.
 */
public record ReturnTargetCreatureToHandAndCreateTokenCopyEffect() implements RemovalEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetPredicates.creature());
    }

    @Override
    public RemovalKind removalKind() {
        return RemovalKind.BOUNCE;
    }
}
