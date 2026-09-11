package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/** Static effect that prevents all combat damage dealt by creatures not matching the exemption. */
public record PreventAllCombatDamageByCreaturesExceptEffect(PermanentPredicate exemptSourcePredicate)
        implements CombatDamageSourceExemptionEffect {
}
