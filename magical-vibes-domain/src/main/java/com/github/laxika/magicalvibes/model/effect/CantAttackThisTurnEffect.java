package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/**
 * Marks creature(s) so they can't attack for the remainder of the turn (a one-shot effect that
 * sets {@code Permanent.cantAttackThisTurn}). Attack-side sibling of {@link CantBlockThisTurnEffect};
 * combine the two for "target creature can't attack or block this turn".
 *
 * <p>Supported scopes: {@link TapUntapScope#TARGET} (target creature, multi-target-group) and
 * {@link TapUntapScope#ALL_CREATURES} (mass, optionally narrowed by {@code filter}).
 *
 * <p>Not to be confused with the static restriction effects ({@code CantAttackUnlessEffect},
 * {@code EnchantedCreatureCantAttackEffect}) or with {@link OtherCreaturesCantAttackThisTurnEffect},
 * which installs a turn-scoped lock exempting only the target.
 *
 * @param scope  which creature(s) can't attack this turn
 * @param filter optional predicate narrowing the scanned scopes (null = no restriction)
 */
public record CantAttackThisTurnEffect(TapUntapScope scope, PermanentPredicate filter) implements CardEffect {

    public CantAttackThisTurnEffect(TapUntapScope scope) {
        this(scope, null);
    }

    @Override
    public TargetSpec targetSpec() {
        return scope == TapUntapScope.TARGET
                ? TargetSpec.benign(TargetPredicates.creature())
                : TargetSpec.NONE;
    }
}
