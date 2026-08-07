package com.github.laxika.magicalvibes.model.effect;

/**
 * Reveal the top card of the controller's library. If it's a creature card, the controller draws
 * cards equal to that card's power and gains life equal to its toughness. The revealed card stays
 * on top of the library, so it is itself the first card drawn when its power is at least 1.
 *
 * <p>Neither {@code CardDrawingEffect} nor {@code LifeGainEffect} is implemented: both amounts
 * depend on a card that is unknown until resolution, so no {@code DynamicAmount} can state them.
 *
 * <p>Used by Nissa's Revelation.
 */
public record RevealTopCardCreatureDrawPowerGainToughnessEffect() implements CardEffect {
}
