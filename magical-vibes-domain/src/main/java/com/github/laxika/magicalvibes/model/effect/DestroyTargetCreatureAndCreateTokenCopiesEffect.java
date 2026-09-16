package com.github.laxika.magicalvibes.model.effect;

/**
 * Destroys the targeted creature, then creates two token copies of it for its controller with
 * their power and toughness set to half the creature's last-known effective values, rounded up.
 */
public record DestroyTargetCreatureAndCreateTokenCopiesEffect() implements RemovalEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.harmful(TargetPredicates.creature());
    }

    @Override
    public RemovalKind removalKind() {
        return RemovalKind.DESTROY;
    }
}
