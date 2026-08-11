package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.amount.DynamicAmount;

/**
 * Prevents the next dynamic amount of damage to one target creature this turn and deals prevented
 * damage to a separate any-target chosen by the spell.
 */
public record PreventXDamageToTargetCreatureAndRedirectToAnyTargetEffect(
        DynamicAmount amount,
        int protectedTargetGroup,
        int redirectTargetGroup
) implements CardEffect {

    public PreventXDamageToTargetCreatureAndRedirectToAnyTargetEffect(DynamicAmount amount) {
        this(amount, 0, 1);
    }

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.harmful(TargetPredicates.anyTarget());
    }
}
