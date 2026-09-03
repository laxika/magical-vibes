package com.github.laxika.magicalvibes.model.effect;

/**
 * Gives the target player permanent control of the permanent enchanted by the source Aura.
 */
public record TargetPlayerGainsControlOfEnchantedPermanentEffect() implements ControlStealingEffect {

    @Override
    public ControlDuration controlDuration() {
        return ControlDuration.PERMANENT;
    }

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetPredicates.player());
    }
}
