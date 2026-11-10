package com.github.laxika.magicalvibes.model.effect;

public record PutCountersOnSourceEffect(int powerModifier, int toughnessModifier, int amount)
        implements CardEffect {

    @Override
    public TargetSpec targetSpec() {
        return new TargetSpec(null, false, null, true, 1);
    }
}
