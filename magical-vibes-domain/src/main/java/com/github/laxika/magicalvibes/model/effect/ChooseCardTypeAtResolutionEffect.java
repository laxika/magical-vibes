package com.github.laxika.magicalvibes.model.effect;

/**
 * "Choose a card type" as a step: the controller chooses as the effect resolves (CR 608.2d) and the resolution keeps
 * the type, read by {@code CardHasCardTypeChosenThisResolutionPredicate} in the steps after it ("for each card of the
 * chosen type revealed this way").
 */
public record ChooseCardTypeAtResolutionEffect() implements CardEffect {
}
