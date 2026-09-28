package com.github.laxika.magicalvibes.model.effect;

/**
 * Chooses a nonland permanent card from the controller's hand and perpetually grants it the
 * configured ETB ability. The concrete ability is supplied by the normal-effect handler because
 * it needs the source card's target restriction.
 */
public record ChooseCardFromHandToPerpetuallyGrantEnterExileEffect() implements CardEffect {
}
