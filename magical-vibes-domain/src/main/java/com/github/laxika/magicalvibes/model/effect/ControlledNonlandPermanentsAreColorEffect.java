package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardColor;

/**
 * Static color-setting effect (CR 105.3, layer 5): every nonland permanent the source's controller
 * controls — the source included — becomes the given color, replacing its other colors. Permanents
 * other players control are unaffected. Controller-scoped, fixed-color sibling of
 * {@link AllNonlandPermanentsAreChosenColorEffect}. Used by Celestial Dawn.
 *
 * @param color the color every controlled nonland permanent becomes
 */
public record ControlledNonlandPermanentsAreColorEffect(CardColor color) implements CardEffect {
}
