package com.github.laxika.magicalvibes.model.effect;

/**
 * Exiles the top cards of the controller's library, then offers one land to put onto the
 * battlefield and at most one spell for each represented nonland card type to cast for free.
 */
public record ExileTopCardsAndMayCastOnePerCardTypeEffect(int count) implements CardEffect {
}
