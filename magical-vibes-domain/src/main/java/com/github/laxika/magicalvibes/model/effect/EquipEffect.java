package com.github.laxika.magicalvibes.model.effect;

public record EquipEffect() implements CardEffect {
    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetCategory.CREATURE);
    }
}
