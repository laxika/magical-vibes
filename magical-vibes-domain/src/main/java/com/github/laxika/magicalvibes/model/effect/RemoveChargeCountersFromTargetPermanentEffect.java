package com.github.laxika.magicalvibes.model.effect;

public record RemoveChargeCountersFromTargetPermanentEffect(int maxCount) implements CardEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetCategory.PERMANENT);
    }
}
