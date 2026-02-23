package com.github.laxika.magicalvibes.model.effect;

public record FirstTargetDealsPowerDamageToSecondTargetEffect() implements CardEffect {

    @Override
    public boolean canTargetPermanent() {
        return true;
    }
}
