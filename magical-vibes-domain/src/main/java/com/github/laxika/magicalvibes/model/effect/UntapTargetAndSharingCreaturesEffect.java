package com.github.laxika.magicalvibes.model.effect;

/**
 * Untaps the target creature and every other creature sharing a color with it. The affected
 * creatures are determined on resolution.
 */
public record UntapTargetAndSharingCreaturesEffect() implements CardEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetPredicates.creature());
    }
}
