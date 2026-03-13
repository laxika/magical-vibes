package com.github.laxika.magicalvibes.model.effect;

/**
 * Wrapper for conditional effects that apply as long as the controller's life total
 * is at or above a specified threshold (e.g. "As long as you have 30 or more life").
 * The wrapped effect only applies while the condition is met.
 */
public record ControllerLifeThresholdConditionalEffect(int lifeThreshold, CardEffect wrapped) implements ConditionalEffect {

    @Override
    public String conditionName() {
        return "life threshold (" + lifeThreshold + "+)";
    }

    @Override
    public String conditionNotMetReason() {
        return "life total is less than " + lifeThreshold;
    }

    @Override
    public boolean canTargetPlayer() {
        return wrapped.canTargetPlayer();
    }

    @Override
    public boolean canTargetPermanent() {
        return wrapped.canTargetPermanent();
    }

    @Override
    public boolean canTargetSpell() {
        return wrapped.canTargetSpell();
    }

    @Override
    public boolean canTargetGraveyard() {
        return wrapped.canTargetGraveyard();
    }
}
