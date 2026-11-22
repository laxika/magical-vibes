package com.github.laxika.magicalvibes.model.effect;

/**
 * The controller chooses odd or even during resolution and the resolution keeps the answer, read by
 * {@code PermanentHasChosenParityPredicate} in the steps after it.
 */
public record ChooseManaValueParityAtResolutionEffect() implements CardEffect {
}
