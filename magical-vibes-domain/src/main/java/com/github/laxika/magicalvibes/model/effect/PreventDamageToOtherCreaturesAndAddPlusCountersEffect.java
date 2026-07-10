package com.github.laxika.magicalvibes.model.effect;

/**
 * Static effect: "If damage would be dealt to another creature you control, prevent that damage.
 * Put a +1/+1 counter on that creature for each 1 damage prevented this way." (e.g. Vigor)
 * <p>
 * Applies to every creature the source's controller controls except the source itself
 * ("another creature"), covering both combat and noncombat damage. Hooked in
 * {@link com.github.laxika.magicalvibes.service.DamagePreventionService#applyCreaturePreventionShield}.
 */
public record PreventDamageToOtherCreaturesAndAddPlusCountersEffect() implements CardEffect {
}
