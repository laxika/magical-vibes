package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

public record GrantActivatedAbilityEffect(
        ActivatedAbility ability,
        GrantScope scope,
        PermanentPredicate filter,
        EffectDuration duration
) implements CardEffect {

    /** Continuous (static) grant — existing behavior. */
    public GrantActivatedAbilityEffect(ActivatedAbility ability, GrantScope scope) {
        this(ability, scope, null, EffectDuration.CONTINUOUS);
    }

    /** Continuous (static) grant with filter — existing behavior. */
    public GrantActivatedAbilityEffect(ActivatedAbility ability, GrantScope scope, PermanentPredicate filter) {
        this(ability, scope, filter, EffectDuration.CONTINUOUS);
    }

    /** TARGET scope grants the ability to the spell/ability's target permanent (e.g. Banishing Knack). */
    @Override
    public TargetSpec targetSpec() {
        return scope == GrantScope.TARGET ? TargetSpec.benign(TargetCategory.PERMANENT) : TargetSpec.NONE;
    }
}
