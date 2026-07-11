package com.github.laxika.magicalvibes.model.effect;

/**
 * Static effect: "Prevent all combat damage that would be dealt to attacking creatures you control." (e.g. Dolmen Gate)
 * <p>
 * Applies to every attacking creature controlled by the source's controller (typically damage from
 * blockers). Noncombat damage is unaffected. Hooked in
 * {@link com.github.laxika.magicalvibes.service.DamagePreventionService#applyCreaturePreventionShield}.
 */
public record PreventCombatDamageToAttackingCreaturesYouControlEffect() implements CardEffect {
}
