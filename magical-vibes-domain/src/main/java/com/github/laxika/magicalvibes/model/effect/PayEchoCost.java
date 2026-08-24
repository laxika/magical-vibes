package com.github.laxika.magicalvibes.model.effect;

/** Echo's printed mana cost, resolved into the applicable payment cost when the trigger resolves. */
public record PayEchoCost(String echoCost) implements CostEffect {
}
