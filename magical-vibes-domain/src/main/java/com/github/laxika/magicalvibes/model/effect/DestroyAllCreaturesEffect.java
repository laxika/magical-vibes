package com.github.laxika.magicalvibes.model.effect;

public record DestroyAllCreaturesEffect(boolean cannotBeRegenerated) implements CardEffect {
}
