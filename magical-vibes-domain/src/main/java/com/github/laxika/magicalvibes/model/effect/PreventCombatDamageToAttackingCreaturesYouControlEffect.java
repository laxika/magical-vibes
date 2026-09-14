package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/**
 * Static effect: "Prevent all combat damage that would be dealt to attacking creatures you control."
 * An optional filter can narrow which attacking creatures are protected (e.g. Goldbug).
 * <p>
 * Applies to matching attacking creatures controlled by the source's controller (typically damage
 * from blockers). Noncombat damage is unaffected. Hooked in
 * {@link com.github.laxika.magicalvibes.service.DamagePreventionService#applyCreaturePreventionShield}.
 */
public record PreventCombatDamageToAttackingCreaturesYouControlEffect(PermanentPredicate filter)
        implements CardEffect {

    /** Unrestricted Dolmen Gate-style prevention. */
    public PreventCombatDamageToAttackingCreaturesYouControlEffect() {
        this(null);
    }
}
