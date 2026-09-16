package com.github.laxika.magicalvibes.model.effect;

/** Reduces the generic cost of every targeted spell and activated ability by {@code amount}. */
public record ReduceTargetedCostEffect(int amount) implements TargetedCostReducingEffect {
}
