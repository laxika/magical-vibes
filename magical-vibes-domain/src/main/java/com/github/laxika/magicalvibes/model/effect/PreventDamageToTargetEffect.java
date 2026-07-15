package com.github.laxika.magicalvibes.model.effect;

public record PreventDamageToTargetEffect(int amount) implements CardEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetCategory.ANY_TARGET);
    }
}
