package com.github.laxika.magicalvibes.model.effect;

/**
 * Causes the source permanent to become a copy of the target land, except it retains the
 * activated ability that produced this effect ("except it has this ability").
 * Used by Thespian's Stage.
 */
public record BecomeCopyOfTargetLandEffect() implements CardEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetPredicates.land());
    }
}
