package com.github.laxika.magicalvibes.model.effect;

/**
 * Wrapper for non-controller-turn conditional static effects.
 * The wrapped effect only applies during turns other than the source permanent's
 * controller's turn (i.e. when the source permanent's controller is not the active player).
 * Used by cards like Warden of the Wall: "During turns other than yours, this artifact
 * is a 2/3 Gargoyle artifact creature with flying."
 */
public record NotControllerTurnConditionalEffect(CardEffect wrapped) implements ConditionalEffect {

    @Override
    public String conditionName() {
        return "turns other than controller's";
    }

    @Override
    public String conditionNotMetReason() {
        return "controller's turn";
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
