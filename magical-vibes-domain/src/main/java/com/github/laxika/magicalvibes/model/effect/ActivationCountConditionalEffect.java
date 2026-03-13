package com.github.laxika.magicalvibes.model.effect;

/**
 * Wrapper for conditional effects that only apply when a specific activated ability
 * on the source permanent has been activated a certain number of times this turn.
 * (e.g. Dragon Whelp: "If this ability has been activated four or more times this turn,
 * sacrifice this creature at the beginning of the next end step.")
 */
public record ActivationCountConditionalEffect(int threshold, int abilityIndex, CardEffect wrapped) implements ConditionalEffect {

    @Override
    public String conditionName() {
        return "activated " + threshold + "+ times";
    }

    @Override
    public String conditionNotMetReason() {
        return "ability has been activated fewer than " + threshold + " times this turn";
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
