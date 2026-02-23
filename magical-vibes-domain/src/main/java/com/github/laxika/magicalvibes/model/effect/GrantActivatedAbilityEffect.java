package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

public record GrantActivatedAbilityEffect(
        ActivatedAbility ability,
        GrantScope scope,
        PermanentPredicate filter
) implements CardEffect {

    public GrantActivatedAbilityEffect(ActivatedAbility ability, GrantScope scope) {
        this(ability, scope, null);
    }
}
