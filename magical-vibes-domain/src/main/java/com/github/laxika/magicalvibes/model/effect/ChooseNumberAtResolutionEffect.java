package com.github.laxika.magicalvibes.model.effect;

/**
 * The controller chooses a number from zero through {@code maxNumber} during resolution and stores
 * it as the resolving stack entry's X value for following effects.
 */
public record ChooseNumberAtResolutionEffect(int maxNumber) implements CardEffect {
}
