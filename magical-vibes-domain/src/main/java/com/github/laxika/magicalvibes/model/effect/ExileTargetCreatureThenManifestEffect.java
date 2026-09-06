package com.github.laxika.magicalvibes.model.effect;

/** Exiles target creature, then its controller manifests the top card of their library. */
public record ExileTargetCreatureThenManifestEffect() implements RemovalEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.harmful(TargetPredicates.creature());
    }

    @Override
    public RemovalKind removalKind() {
        return RemovalKind.EXILE;
    }
}
