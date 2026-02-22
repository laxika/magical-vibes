package com.github.laxika.magicalvibes.model.effect;

public record PutCountersOnSourceEffect(int powerModifier, int toughnessModifier, int amount) implements CardEffect {
}
