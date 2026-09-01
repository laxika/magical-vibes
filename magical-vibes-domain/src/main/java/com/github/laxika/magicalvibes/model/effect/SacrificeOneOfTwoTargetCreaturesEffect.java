package com.github.laxika.magicalvibes.model.effect;

/**
 * "Choose two target creatures controlled by the same player. That player sacrifices one of them
 * of their choice."
 */
public record SacrificeOneOfTwoTargetCreaturesEffect() implements RemovalEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.harmful(TargetPredicates.creature());
    }

    /** Not single-target removal: two creatures are targeted and only one is sacrificed. */
    @Override
    public RemovalKind removalKind() {
        return null;
    }
}
