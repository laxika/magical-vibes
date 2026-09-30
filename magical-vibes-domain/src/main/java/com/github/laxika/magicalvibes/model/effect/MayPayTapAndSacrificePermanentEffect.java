package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/**
 * Resolution-time choice to tap permanents and sacrifice one matching permanent before the
 * follow-up effect is put on the stack.
 *
 * @param tapCost the untapped permanents to tap
 * @param sacrificeFilter permanents that may be sacrificed
 * @param thenEffect effect put on the stack after the sacrifice
 * @param permanentDescription human-readable description of the sacrifice choice
 * @param prompt human-readable payment prompt
 */
public record MayPayTapAndSacrificePermanentEffect(
        TapMultiplePermanentsCost tapCost,
        PermanentPredicate sacrificeFilter,
        CardEffect thenEffect,
        String permanentDescription,
        String prompt
) implements CardEffect {

    @Override
    public TargetSpec targetSpec() {
        return thenEffect == null ? TargetSpec.NONE : thenEffect.targetSpec();
    }
}
