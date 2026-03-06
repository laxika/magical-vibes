package com.github.laxika.magicalvibes.model.effect;

public record RemoveChargeCountersFromTargetPermanentEffect(int maxCount) implements CardEffect {

    @Override
    public boolean canTargetPermanent() {
        return true;
    }
}
