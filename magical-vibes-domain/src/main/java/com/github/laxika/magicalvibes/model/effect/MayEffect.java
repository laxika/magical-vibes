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
}
