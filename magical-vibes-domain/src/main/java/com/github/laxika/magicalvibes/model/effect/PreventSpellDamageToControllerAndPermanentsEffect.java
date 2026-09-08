package com.github.laxika.magicalvibes.model.effect;

/**
 * Static replacement effect that prevents damage dealt by spells to this permanent's controller
 * or to a permanent that controller controls.
 */
public record PreventSpellDamageToControllerAndPermanentsEffect()
        implements SpellDamagePreventionEffect {
}
