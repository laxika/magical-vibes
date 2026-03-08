package com.github.laxika.magicalvibes.model.effect;

/**
 * Wrapper for "as long as an opponent is poisoned" conditional static effects.
 * The wrapped effect only applies if any opponent of the controller has at least
 * one poison counter. Used for static keyword grants like Viridian Betrayers.
 */
public record OpponentPoisonedConditionalEffect(CardEffect wrapped) implements ConditionalEffect {

    @Override
    public String conditionName() {
        return "opponent poisoned";
    }

    @Override
    public String conditionNotMetReason() {
        return "no opponent is poisoned";
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
