package com.github.laxika.magicalvibes.model.effect;

public record DestroyTargetPermanentAndGiveControllerPoisonCountersEffect(int poisonCounters) implements CardEffect {

    public DestroyTargetPermanentAndGiveControllerPoisonCountersEffect() {
        this(1);
    }

    @Override
    public boolean canTargetPermanent() {
        return true;
    }
}
