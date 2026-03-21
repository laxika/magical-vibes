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
}
