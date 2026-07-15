package com.github.laxika.magicalvibes.model.effect;

public record DealXDamageToAnyTargetAndGainXLifeEffect() implements CardEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.harmful(TargetCategory.ANY_TARGET);
    }
}
