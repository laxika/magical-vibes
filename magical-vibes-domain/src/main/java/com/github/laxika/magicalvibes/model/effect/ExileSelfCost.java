package com.github.laxika.magicalvibes.model.effect;

public record ExileSelfCost(boolean trackWithSource) implements CostEffect {

    public ExileSelfCost() {
        this(false);
    }
}
