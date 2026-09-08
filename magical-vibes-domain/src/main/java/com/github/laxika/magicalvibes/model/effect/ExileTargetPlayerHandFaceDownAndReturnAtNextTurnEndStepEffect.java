package com.github.laxika.magicalvibes.model.effect;

/** Exiles the target player's hand face down and returns those cards at that player's next turn's end step. */
public record ExileTargetPlayerHandFaceDownAndReturnAtNextTurnEndStepEffect() implements CardEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.harmful(TargetPredicates.player());
    }
}
