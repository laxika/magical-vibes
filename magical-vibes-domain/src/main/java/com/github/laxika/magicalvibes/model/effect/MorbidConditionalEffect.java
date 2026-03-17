package com.github.laxika.magicalvibes.model.effect;

/**
 * Wrapper for morbid conditional effects (ETB triggers and static abilities).
 * The wrapped effect only triggers/resolves/applies if a creature died this turn.
 * For ETB triggers, the condition is checked both when the trigger would be placed
 * on the stack and again at resolution time (intervening-if).
 */
public record MorbidConditionalEffect(CardEffect wrapped) implements ConditionalEffect {

    @Override
    public String conditionName() {
        return "morbid";
    }

    @Override
    public String conditionNotMetReason() {
        return "no creature died this turn";
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
