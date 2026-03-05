package com.github.laxika.magicalvibes.model.effect;

/**
 * Static effect: creatures you control of the permanent's chosen color get +1/+1.
 * Used by Caged Sun and similar cards that choose a color as they enter the battlefield.
 * The chosen color is stored on the source permanent at runtime via {@code Permanent.getChosenColor()}.
 */
public record BoostCreaturesOfChosenColorEffect(int powerBoost, int toughnessBoost) implements CardEffect {
}
