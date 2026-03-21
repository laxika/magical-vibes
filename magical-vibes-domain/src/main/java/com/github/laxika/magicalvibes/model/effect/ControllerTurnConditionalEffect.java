package com.github.laxika.magicalvibes.model.effect;

/**
 * Wrapper for controller-turn conditional static effects.
 * The wrapped effect only applies during the controller's turn (i.e. when the
 * source permanent's controller is the active player).
 * Used by cards like Jousting Lance: "During your turn, equipped creature has first strike."
 */
public record ControllerTurnConditionalEffect(CardEffect wrapped) implements ConditionalEffect {

    @Override
    public String conditionName() {
        return "controller's turn";
    }

    @Override
    public String conditionNotMetReason() {
        return "not controller's turn";
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
