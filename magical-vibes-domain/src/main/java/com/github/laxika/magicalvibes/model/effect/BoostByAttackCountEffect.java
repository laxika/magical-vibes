package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

import java.util.Set;

/** Continuous boost based on how many times each affected creature attacked. */
public record BoostByAttackCountEffect(
        int powerPerAttack,
        int toughnessPerAttack,
        PermanentSubject subject,
        PermanentPredicate filter,
        boolean countThisGame
) implements StaticCreatureBoostEffect {

    public BoostByAttackCountEffect(int powerPerAttack, int toughnessPerAttack, PermanentSubject subject) {
        this(powerPerAttack, toughnessPerAttack, subject, null, false);
    }

    public BoostByAttackCountEffect(int powerPerAttack, int toughnessPerAttack,
                                    PermanentSubject subject, PermanentPredicate filter) {
        this(powerPerAttack, toughnessPerAttack, subject, filter, false);
    }

    public static BoostByAttackCountEffect forEachAttackThisGame(int powerPerAttack, int toughnessPerAttack,
                                                                  PermanentSubject subject) {
        return new BoostByAttackCountEffect(powerPerAttack, toughnessPerAttack, subject, null, true);
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
