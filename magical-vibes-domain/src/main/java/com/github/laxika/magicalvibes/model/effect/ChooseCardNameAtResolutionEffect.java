package com.github.laxika.magicalvibes.model.effect;

/**
 * Asks the controller to choose a card name while the effect resolves and stores that name on
 * the source permanent.
 */
public record ChooseCardNameAtResolutionEffect() implements CardEffect {
}
