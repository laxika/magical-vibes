package com.github.laxika.magicalvibes.model.effect;

public record GainControlOfEnchantedTargetEffect() implements CardEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetCategory.CREATURE);
    }
}
