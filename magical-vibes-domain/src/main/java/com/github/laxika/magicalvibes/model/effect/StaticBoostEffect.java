package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

import java.util.Set;

public record StaticBoostEffect(
        int powerBoost,
        int toughnessBoost,
        Set<Keyword> grantedKeywords,
        GrantScope scope,
        PermanentPredicate filter
) implements CardEffect {

    public StaticBoostEffect(int powerBoost, int toughnessBoost, Set<Keyword> grantedKeywords, GrantScope scope) {
        this(powerBoost, toughnessBoost, grantedKeywords, scope, null);
    }

    public StaticBoostEffect(int powerBoost, int toughnessBoost, GrantScope scope, PermanentPredicate filter) {
        this(powerBoost, toughnessBoost, Set.of(), scope, filter);
    }

    public StaticBoostEffect(int powerBoost, int toughnessBoost, GrantScope scope) {
        this(powerBoost, toughnessBoost, Set.of(), scope, null);
    }
}
