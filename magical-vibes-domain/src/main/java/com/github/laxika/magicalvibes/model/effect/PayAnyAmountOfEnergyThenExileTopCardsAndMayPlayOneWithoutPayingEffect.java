package com.github.laxika.magicalvibes.model.effect;

/**
 * Pays any amount of energy, then, if at least one energy was paid, shuffles the controller's
 * library, exiles that many cards from its top, and offers one of them for free play.
 */
public record PayAnyAmountOfEnergyThenExileTopCardsAndMayPlayOneWithoutPayingEffect()
        implements CardEffect {
}
