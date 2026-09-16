package com.github.laxika.magicalvibes.model.effect;

/** Gives the player carried by the stack entry control of the source permanent until end of turn. */
public record TargetPlayerGainsControlOfSourcePermanentUntilEndOfTurnEffect()
        implements CardEffect, ControlStealingEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetPredicates.player());
    }

    @Override
    public ControlDuration controlDuration() {
        return ControlDuration.END_OF_TURN;
    }
}
