package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

import java.util.Set;

/**
 * Continuous boost based on how many times each affected creature attacked this turn.
 */
public record BoostByAttackCountEffect(
        int powerPerAttack,
        int toughnessPerAttack,
        GrantScope scope,
        PermanentPredicate filter
) implements StaticCreatureBoostEffect {

    public BoostByAttackCountEffect(int powerPerAttack, int toughnessPerAttack, GrantScope scope) {
        this(powerPerAttack, toughnessPerAttack, scope, null);
    }

    @Override
    public int powerBoost() {
        return powerPerAttack;
    }

    @Override
    public int toughnessBoost() {
        return toughnessPerAttack;
    }

    @Override
    public Set<Keyword> grantedKeywords() {
        return Set.of();
    }
}
