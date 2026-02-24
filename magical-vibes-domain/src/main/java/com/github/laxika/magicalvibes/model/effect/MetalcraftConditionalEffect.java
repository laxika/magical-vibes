package com.github.laxika.magicalvibes.model.effect;

/**
 * Wrapper for metalcraft "intervening-if" ETB triggers.
 * The wrapped effect only triggers/resolves if the controller controls 3+ artifacts.
 * Delegates targeting to the wrapped effect so target selection works at cast time.
 */
public record MetalcraftConditionalEffect(CardEffect wrapped) implements CardEffect {

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
