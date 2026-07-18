package com.github.laxika.magicalvibes.model.effect;

/**
 * Static effect: "Prevent all damage that would be dealt to this creature by creatures." (e.g. Uncle Istvan)
 * <p>
 * Prevents all damage — combat and noncombat — dealt to the source permanent whenever the damage's
 * source is a creature. Combat damage is always dealt by a creature (CR 510.1c) and is prevented in
 * {@link com.github.laxika.magicalvibes.service.DamagePreventionService#applyCreaturePreventionShield};
 * the noncombat case (a creature's ability, fight, etc.) is checked in
 * {@code DamageSupport.dealCreatureDamage} via
 * {@code GameQueryService.isCreatureSourceDamageToSelfPrevented}. Damage from noncreature sources
 * (spells like Shock, artifacts, etc.) is unaffected.
 */
public record PreventDamageToSelfFromCreaturesEffect() implements CardEffect {
}
