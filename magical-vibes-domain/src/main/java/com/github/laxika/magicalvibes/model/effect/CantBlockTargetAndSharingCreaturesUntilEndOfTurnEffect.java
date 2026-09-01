package com.github.laxika.magicalvibes.model.effect;

/**
 * Marks the target creature and every other creature sharing a color with it so they can't block
 * this turn. The affected creatures are determined on resolution.
 */
public record CantBlockTargetAndSharingCreaturesUntilEndOfTurnEffect() implements CardEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetPredicates.creature());
    }
}
