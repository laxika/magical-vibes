package com.github.laxika.magicalvibes.model.effect;

/** Gives the target player control of the permanent enchanted by the source until end of turn. */
public record TargetPlayerGainsControlOfEnchantedPermanentUntilEndOfTurnEffect()
        implements ControlStealingEffect {

    @Override
    public ControlDuration controlDuration() {
        return ControlDuration.END_OF_TURN;
    }

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetPredicates.player());
    }
}
