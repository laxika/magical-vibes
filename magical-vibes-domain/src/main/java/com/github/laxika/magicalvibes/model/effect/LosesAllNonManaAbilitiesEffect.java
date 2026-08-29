package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/**
 * Causes permanents matching the given scope to lose all abilities except mana abilities.
 * This is a continuous layer-6 effect used by cards such as Blood Sun.
 *
 * @param scope  which permanents are affected
 * @param filter optional additional permanent filter
 */
public record LosesAllNonManaAbilitiesEffect(GrantScope scope, PermanentPredicate filter)
        implements CardEffect {

    public LosesAllNonManaAbilitiesEffect(GrantScope scope) {
        this(scope, null);
    }

    @Override
    public TargetSpec targetSpec() {
        return scope == GrantScope.TARGET ? TargetSpec.benign(TargetPredicates.permanent()) : TargetSpec.NONE;
    }
}
