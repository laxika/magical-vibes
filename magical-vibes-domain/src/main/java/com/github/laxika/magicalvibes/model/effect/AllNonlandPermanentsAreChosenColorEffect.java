package com.github.laxika.magicalvibes.model.effect;

/**
 * Static color-setting effect (CR 105.3 / layer 5): every nonland permanent on the battlefield,
 * regardless of controller, becomes the source permanent's chosen color, replacing its other
 * colors. The chosen color is stored on the source at runtime via {@link
 * com.github.laxika.magicalvibes.model.Permanent#getChosenColor()} (pair with
 * {@link ChooseColorOnEnterEffect}). Used by Shifting Sky.
 */
public record AllNonlandPermanentsAreChosenColorEffect() implements CardEffect {
}
