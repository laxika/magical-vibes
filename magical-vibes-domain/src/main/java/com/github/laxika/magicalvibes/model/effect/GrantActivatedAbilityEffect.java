package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

import java.util.UUID;

public record GrantActivatedAbilityEffect(
        ActivatedAbility ability,
        GrantScope scope,
        PermanentPredicate filter,
        EffectDuration duration,
        UUID expirationCardId
) implements CardEffect {

    /** Continuous (static) grant — existing behavior. */
    public GrantActivatedAbilityEffect(ActivatedAbility ability, GrantScope scope) {
        this(ability, scope, null, EffectDuration.CONTINUOUS, null);
    }

    /** Continuous (static) grant with filter — existing behavior. */
    public GrantActivatedAbilityEffect(ActivatedAbility ability, GrantScope scope, PermanentPredicate filter) {
        this(ability, scope, filter, EffectDuration.CONTINUOUS, null);
    }

    public GrantActivatedAbilityEffect(ActivatedAbility ability, GrantScope scope,
                                       PermanentPredicate filter, EffectDuration duration) {
        this(ability, scope, filter, duration, null);
    }

    /** TARGET scope grants the ability to the spell/ability's target permanent (e.g. Banishing Knack). */
    @Override
    public TargetSpec targetSpec() {
        return scope == GrantScope.TARGET ? TargetSpec.benign(TargetPredicates.permanent()) : TargetSpec.NONE;
    }
}
