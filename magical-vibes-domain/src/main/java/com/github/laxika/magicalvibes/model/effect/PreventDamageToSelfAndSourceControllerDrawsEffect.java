package com.github.laxika.magicalvibes.model.effect;

/**
 * Static effect: "If a source would deal damage to this creature, prevent that damage.
 * The source's controller draws cards equal to the damage prevented this way." (Swans of Bryn Argoll)
 * <p>
 * Applies to all damage (combat and noncombat, any source) dealt to the permanent carrying this
 * effect. Hooked in
 * {@link com.github.laxika.magicalvibes.service.DamagePreventionService#applySwansSourceControllerDraw}.
 */
public record PreventDamageToSelfAndSourceControllerDrawsEffect() implements CardEffect {
}
