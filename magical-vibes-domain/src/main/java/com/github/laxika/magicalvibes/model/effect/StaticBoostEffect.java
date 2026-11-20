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
 * When {@code scalingCounterOnTarget} is true, the counter count is read from each affected
 * permanent instead of the source (Thelon of Havenwood).
 */
public record StaticBoostEffect(
        int powerBoost,
        int toughnessBoost,
        Set<Keyword> grantedKeywords,
        PermanentSubject subject,
        PermanentPredicate filter,
        CounterType scalingCounter,
        boolean scalingCounterOnTarget
) implements StaticCreatureBoostEffect {

    public StaticBoostEffect(int powerBoost, int toughnessBoost, Set<Keyword> grantedKeywords,
                             PermanentSubject subject) {
        this(powerBoost, toughnessBoost, grantedKeywords, subject, null, null, false);
    }

    public StaticBoostEffect(int powerBoost, int toughnessBoost, Set<Keyword> grantedKeywords, PermanentSubject subject,
                             PermanentPredicate filter) {
        this(powerBoost, toughnessBoost, grantedKeywords, subject, filter, null, false);
    }

    public StaticBoostEffect(int powerBoost, int toughnessBoost, PermanentSubject subject, PermanentPredicate filter) {
        this(powerBoost, toughnessBoost, Set.of(), subject, filter, null, false);
    }

    public StaticBoostEffect(int powerBoost, int toughnessBoost, Set<Keyword> grantedKeywords,
                             PermanentSubject subject, PermanentPredicate filter, CounterType scalingCounter) {
        this(powerBoost, toughnessBoost, grantedKeywords, subject, filter, scalingCounter, false);
    }

    public StaticBoostEffect(int powerBoost, int toughnessBoost, PermanentSubject subject) {
        this(powerBoost, toughnessBoost, Set.of(), subject, null, null, false);
    }

    public StaticBoostEffect(int powerBoost, int toughnessBoost, PermanentSubject subject, CounterType scalingCounter) {
        this(powerBoost, toughnessBoost, Set.of(), subject, null, scalingCounter, false);
    }

    public StaticBoostEffect(int powerBoost, int toughnessBoost, PermanentSubject subject,
                             PermanentPredicate filter, CounterType scalingCounter,
                             boolean scalingCounterOnTarget) {
        this(powerBoost, toughnessBoost, Set.of(), subject, filter, scalingCounter, scalingCounterOnTarget);
    }
}
