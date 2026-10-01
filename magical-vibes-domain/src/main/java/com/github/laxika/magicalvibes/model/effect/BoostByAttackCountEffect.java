package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

import java.util.Set;

/** Continuous boost based on how many times each affected creature attacked. */
public record BoostByAttackCountEffect(
        int powerPerAttack,
        int toughnessPerAttack,
        GrantScope scope,
        PermanentPredicate filter,
        boolean countThisGame
) implements StaticCreatureBoostEffect {

    public BoostByAttackCountEffect(int powerPerAttack, int toughnessPerAttack, GrantScope scope) {
        this(powerPerAttack, toughnessPerAttack, scope, null, false);
    }

    public BoostByAttackCountEffect(int powerPerAttack, int toughnessPerAttack,
                                    GrantScope scope, PermanentPredicate filter) {
        this(powerPerAttack, toughnessPerAttack, scope, filter, false);
    }

    public static BoostByAttackCountEffect forEachAttackThisGame(int powerPerAttack, int toughnessPerAttack,
                                                                  GrantScope scope) {
        return new BoostByAttackCountEffect(powerPerAttack, toughnessPerAttack, scope, null, true);
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
