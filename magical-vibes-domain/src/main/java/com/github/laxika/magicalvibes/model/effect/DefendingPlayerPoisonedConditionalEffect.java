package com.github.laxika.magicalvibes.model.effect;

/**
 * Wrapper for "if defending player is poisoned" conditional effects.
 * The wrapped effect only resolves if the defending player (opponent of the controller)
 * has at least one poison counter. Used for intervening-if attack triggers like Septic Rats.
 */
public record DefendingPlayerPoisonedConditionalEffect(CardEffect wrapped) implements ConditionalEffect {

    @Override
    public String conditionName() {
        return "defending player poisoned";
    }

    @Override
    public String conditionNotMetReason() {
        return "defending player is not poisoned";
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
