package com.github.laxika.magicalvibes.model.effect;

public record DestroyTargetLandAndDamageControllerEffect(int damage) implements CardEffect {

    @Override
    public boolean canTargetPermanent() {
        return true;
    }

    @Override
    public boolean isDamageOrDestruction() {
        return true;
    }
}
