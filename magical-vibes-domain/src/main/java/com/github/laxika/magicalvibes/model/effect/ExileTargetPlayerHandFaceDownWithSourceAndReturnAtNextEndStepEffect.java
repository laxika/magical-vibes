package com.github.laxika.magicalvibes.model.effect;

/** Exiles the target player's hand face down with the source and returns those cards at the next end step. */
public record ExileTargetPlayerHandFaceDownWithSourceAndReturnAtNextEndStepEffect() implements CardEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.harmful(TargetPredicates.player());
    }
}
