package com.github.laxika.magicalvibes.model.effect;

public record DestroyCreatureBlockingThisEffect() implements CardEffect {

    @Override
    public boolean canTargetPermanent() {
        return true;
    }

    @Override
    public boolean isDamageOrDestruction() {
        return true;
    }
}
