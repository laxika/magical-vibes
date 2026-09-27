package com.github.laxika.magicalvibes.model.effect;

/**
 * Gives the target player in the preceding target group control of the permanents in the
 * following target group until end of turn.
 */
public record TargetPlayerGainsControlOfTargetPermanentsUntilEndOfTurnEffect(
        int permanentTargetGroupIndex) implements ControlStealingEffect {

    @Override
    public ControlDuration controlDuration() {
        return ControlDuration.END_OF_TURN;
    }

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetPredicates.permanent());
    }
}
