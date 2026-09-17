package com.github.laxika.magicalvibes.model.effect;

/**
 * Destroys the targeted creature, then creates two token copies of it with its last-known power
 * and toughness each halved and rounded up.
 */
public record DestroyTargetCreatureThenCreateHalfTokenCopiesEffect() implements RemovalEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.harmful(TargetPredicates.creature());
    }

    @Override
    public RemovalKind removalKind() {
        return RemovalKind.DESTROY;
    }
}
