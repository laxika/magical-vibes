package com.github.laxika.magicalvibes.model.effect;

/**
 * Additional cast cost that asks the caster to choose X from an inclusive range.
 * The choice itself consumes no resource; it is carried by the spell's stack-entry X value.
 */
public record ChooseXValueCost(int minValue, int maxValue) implements CostEffect {

    public ChooseXValueCost {
        if (minValue < 0 || maxValue < minValue) {
            throw new IllegalArgumentException("Invalid X value range");
        }
    }
}
