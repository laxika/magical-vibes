package com.github.laxika.magicalvibes.model.effect;

public record DestroyTargetLandAndDamageControllerEffect(int damage) implements CardEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.harmful(TargetCategory.LAND);
    }
}
