package com.github.laxika.magicalvibes.model.effect;

/**
 * Gives the target player permanent control of every other permanent controlled by the ability
 * controller at resolution.
 */
public record TargetPlayerGainsControlOfAllOtherPermanentsYouControlEffect() implements CardEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetPredicates.player());
    }
}
