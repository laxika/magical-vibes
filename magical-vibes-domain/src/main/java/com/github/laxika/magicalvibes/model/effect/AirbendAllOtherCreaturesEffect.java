package com.github.laxika.magicalvibes.model.effect;

/** Airbends every creature except the optional target chosen for this effect. */
public record AirbendAllOtherCreaturesEffect() implements RemovalEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.harmful(TargetPredicates.creature());
    }

    @Override
    public RemovalKind removalKind() {
        return RemovalKind.EXILE;
    }

    @Override
    public boolean resolvesWhenTargetIllegal() {
        return true;
    }
}
