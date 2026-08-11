package com.github.laxika.magicalvibes.model.effect;

/**
 * Static effect: prevent damage dealt to a creature by another creature when the two creatures
 * share a color.
 */
public record PreventDamageToCreaturesByCreaturesOfSharedColorEffect()
        implements SharedColorDamagePreventionEffect {
}
