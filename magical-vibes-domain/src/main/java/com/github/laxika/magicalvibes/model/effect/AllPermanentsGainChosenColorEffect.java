package com.github.laxika.magicalvibes.model.effect;

/**
 * Static color-setting effect (CR 105.3 / layer 5): every permanent on the battlefield, regardless
 * of controller and including lands and the source itself, gains the source permanent's chosen
 * color <em>in addition to</em> its other colors (additive, not replacing — contrast {@link
 * AllNonlandPermanentsAreChosenColorEffect}). The chosen color is stored on the source at runtime
 * via {@link com.github.laxika.magicalvibes.model.Permanent#getChosenColor()} (pair with
 * {@link ChooseColorOnEnterEffect}). Used by Painter's Servant.
 */
public record AllPermanentsGainChosenColorEffect() implements CardEffect {
}
