package com.github.laxika.magicalvibes.model.effect;

/**
 * Static effect: creatures you control of the permanent's chosen subtype get +P/+T.
 * Used by Vanquisher's Banner and similar cards that choose a creature type as they enter
 * the battlefield. The chosen subtype is stored on the source permanent at runtime via
 * {@code Permanent.getChosenSubtype()}.
 */
public record BoostCreaturesOfChosenSubtypeEffect(int powerBoost, int toughnessBoost) implements CardEffect {
}
