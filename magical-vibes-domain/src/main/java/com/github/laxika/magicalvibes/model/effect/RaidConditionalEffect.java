package com.github.laxika.magicalvibes.model.effect;

/**
 * Wrapper for raid conditional effects (ETB triggers).
 * The wrapped effect only triggers/resolves if the controller attacked with a creature this turn.
 * For ETB triggers, delegates targeting to the wrapped effect so target selection works at cast time.
 */
public record RaidConditionalEffect(CardEffect wrapped) implements ConditionalEffect {

    @Override
    public String conditionName() {
        return "raid";
    }

    @Override
    public String conditionNotMetReason() {
        return "you didn't attack this turn";
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
