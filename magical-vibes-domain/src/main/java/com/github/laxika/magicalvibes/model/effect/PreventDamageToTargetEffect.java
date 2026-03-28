package com.github.laxika.magicalvibes.model.effect;

public record PreventDamageToTargetEffect(int amount) implements CardEffect {

    @Override
    public boolean canTargetPlayer() {
        return true;
    }

    @Override
    public boolean canTargetPermanent() {
        return true;
    }
}
