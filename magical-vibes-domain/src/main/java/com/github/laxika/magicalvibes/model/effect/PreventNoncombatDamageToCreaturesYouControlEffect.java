package com.github.laxika.magicalvibes.model.effect;

/**
 * Static effect: "Prevent all noncombat damage that would be dealt to creatures you control." (e.g. Mark of Asylum)
 * <p>
 * Applies to every creature controlled by the source's controller (evaluated when damage would be
 * dealt, so it covers creatures gained after this resolves). Combat damage is unaffected. Hooked in
 * {@link com.github.laxika.magicalvibes.service.DamagePreventionService#applyCreaturePreventionShield}.
 */
public record PreventNoncombatDamageToCreaturesYouControlEffect() implements CardEffect {
}
