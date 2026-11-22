package com.github.laxika.magicalvibes.model.effect;

/**
 * "Choose a creature type" as a step: the controller chooses as the effect resolves (CR 608.2d) and the resolution
 * keeps the type, read by {@code PermanentHasTypeChosenThisResolutionPredicate} and
 * {@code CardHasTypeChosenThisResolutionPredicate} in the steps after it.
 */
public record ChooseCreatureTypeAtResolutionEffect() implements CardEffect {
}
