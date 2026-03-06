package com.github.laxika.magicalvibes.model.effect;

public record DestroyTargetPermanentAndControllerLosesLifeEffect(int lifeLoss) implements CardEffect {

    @Override
    public boolean canTargetPermanent() {
        return true;
    }
}
