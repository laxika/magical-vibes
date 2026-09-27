package com.github.laxika.magicalvibes.model.effect;

/**
 * Destroys the targeted creature, then creates two token copies under its controller if it died
 * this way. The copies have half the creature's last-known effective power and toughness, rounded
 * up.
 */
public record DestroyTargetCreatureThenCreateTokenCopiesWithHalfPowerToughnessEffect()
        implements RemovalEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.harmful(TargetPredicates.creature());
    }

    @Override
    public RemovalKind removalKind() {
        return RemovalKind.DESTROY;
    }
}
