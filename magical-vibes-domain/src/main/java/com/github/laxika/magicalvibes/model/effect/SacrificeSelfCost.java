package com.github.laxika.magicalvibes.model.effect;

public record SacrificeSelfCost() implements CostEffect {

    @Override
    public boolean consumesSourcePermanent() {
        return true;
    }
}
