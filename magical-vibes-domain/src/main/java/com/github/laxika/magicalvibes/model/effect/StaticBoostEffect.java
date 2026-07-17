package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

import java.util.Set;

/**
 * Continuous anthem/aura boost. When {@code scalingCounter} is non-null the flat
 * {@code powerBoost}/{@code toughnessBoost} is multiplied by the number of counters of that type on
 * the source permanent ("All creatures get +1/+0 for each time counter on this artifact" — Infinite
 * Hourglass). A {@code null} value applies the flat boost.
 */
public record StaticBoostEffect(
        int powerBoost,
        int toughnessBoost,
        Set<Keyword> grantedKeywords,
        GrantScope scope,
        PermanentPredicate filter,
        CounterType scalingCounter
) implements StaticCreatureBoostEffect {

    public StaticBoostEffect(int powerBoost, int toughnessBoost, Set<Keyword> grantedKeywords, GrantScope scope) {
        this(powerBoost, toughnessBoost, grantedKeywords, scope, null, null);
    }

    public StaticBoostEffect(int powerBoost, int toughnessBoost, Set<Keyword> grantedKeywords, GrantScope scope,
                             PermanentPredicate filter) {
        this(powerBoost, toughnessBoost, grantedKeywords, scope, filter, null);
    }

    public StaticBoostEffect(int powerBoost, int toughnessBoost, GrantScope scope, PermanentPredicate filter) {
        this(powerBoost, toughnessBoost, Set.of(), scope, filter, null);
    }

    public StaticBoostEffect(int powerBoost, int toughnessBoost, GrantScope scope) {
        this(powerBoost, toughnessBoost, Set.of(), scope, null, null);
    }

    public StaticBoostEffect(int powerBoost, int toughnessBoost, GrantScope scope, CounterType scalingCounter) {
        this(powerBoost, toughnessBoost, Set.of(), scope, null, scalingCounter);
    }
}
