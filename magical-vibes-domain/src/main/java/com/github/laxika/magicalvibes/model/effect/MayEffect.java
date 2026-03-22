package com.github.laxika.magicalvibes.model.effect;

public record MayEffect(CardEffect wrapped, String prompt) implements CardEffect {

    @Override
    public boolean canTargetPermanent() {
        return wrapped.canTargetPermanent();
    }

    @Override
    public boolean canTargetPlayer() {
        return wrapped.canTargetPlayer();
    }

    @Override
    public boolean canTargetSpell() {
        return wrapped.canTargetSpell();
    }

    @Override
    public boolean canTargetGraveyard() {
        return wrapped.canTargetGraveyard();
    }

    @Override
    public int requiredPlayerTargetCount() {
        return wrapped.requiredPlayerTargetCount();
    }
}
