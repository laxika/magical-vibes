package com.github.laxika.magicalvibes.model.effect;

/**
 * Wrapper for metalcraft conditional effects (both ETB triggers and static abilities).
 * The wrapped effect only triggers/resolves/applies if the controller controls 3+ artifacts.
 * For ETB triggers, delegates targeting to the wrapped effect so target selection works at cast time.
 * For static effects, the wrapped effect (e.g. GrantKeywordEffect, StaticBoostEffect) is applied
 * only while the metalcraft condition is met.
 */
public record MetalcraftConditionalEffect(CardEffect wrapped) implements ConditionalEffect {

    @Override
    public String conditionName() {
        return "metalcraft";
    }

    @Override
    public String conditionNotMetReason() {
        return "fewer than three artifacts";
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
