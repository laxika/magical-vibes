package com.github.laxika.magicalvibes.model.effect;

/**
 * Conditional wrapper that only applies when the controller's life total
 * is at or below a specified threshold (e.g. "if you have 5 or less life").
 * Used as an intervening-if condition on triggered abilities.
 */
public record ControllerLifeAtOrBelowThresholdConditionalEffect(int lifeThreshold, CardEffect wrapped) implements ConditionalEffect {

    @Override
    public String conditionName() {
        return "life at or below " + lifeThreshold;
    }

    @Override
    public String conditionNotMetReason() {
        return "life total is greater than " + lifeThreshold;
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
