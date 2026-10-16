package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/**
 * Deal {@code damage} to each permanent matching {@code predicate} across the battlefields
 * selected by {@code scope}. The amount is any {@link DynamicAmount} — a constant ({@link Fixed})
 * or a derived value such as "the number of Giants you control"
 * ({@code PermanentCount(giant, CONTROLLER)}, Thundercloud Shaman).
 *
 * <p>Covers "each attacking creature" ({@link EachPermanentScope#ALL_PLAYERS} + an attacking-creature
 * predicate), "each creature target player controls" ({@link EachPermanentScope#TARGET_PLAYER}) and
 * filtered subsets such as "each attacking or blocking creature target player controls".</p>
 *
 * <p>With {@code onlyDeclaredAttackers} the creatures damaged are the ones the attack trigger
 * recorded when it triggered ("each of those creatures", Lightmine Field), whether or not they are
 * still attacking; the predicate then only feeds the amount.</p>
 */
public record DealDamageToEachMatchingPermanentEffect(DynamicAmount damage, PermanentPredicate predicate,
                                                      EachPermanentScope scope, boolean onlyDeclaredAttackers)
        implements CardEffect {

    public DealDamageToEachMatchingPermanentEffect(DynamicAmount damage, PermanentPredicate predicate,
                                                   EachPermanentScope scope) {
        this(damage, predicate, scope, false);
    }

    public DealDamageToEachMatchingPermanentEffect(int damage, PermanentPredicate predicate,
                                                   EachPermanentScope scope) {
        this(new Fixed(damage), predicate, scope, false);
    }

    @Override
    public TargetSpec targetSpec() {
        // Always harmful (deals damage) — canTargetPlayer only for the TARGET_PLAYER scope.
        return scope == EachPermanentScope.TARGET_PLAYER
                ? TargetSpec.harmful(TargetPredicates.player())
                : new TargetSpec(null, true, null, false, 1);
    }
}
