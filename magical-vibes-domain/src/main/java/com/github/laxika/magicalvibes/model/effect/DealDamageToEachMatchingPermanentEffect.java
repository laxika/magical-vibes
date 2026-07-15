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
 */
public record DealDamageToEachMatchingPermanentEffect(DynamicAmount damage, PermanentPredicate predicate,
                                                      EachPermanentScope scope) implements CardEffect {

    public DealDamageToEachMatchingPermanentEffect(int damage, PermanentPredicate predicate,
                                                   EachPermanentScope scope) {
        this(new Fixed(damage), predicate, scope);
    }

    @Override
    public TargetSpec targetSpec() {
        // Always harmful (deals damage) — canTargetPlayer only for the TARGET_PLAYER scope.
        return scope == EachPermanentScope.TARGET_PLAYER
                ? TargetSpec.harmful(TargetCategory.PLAYER)
                : new TargetSpec(TargetCategory.NONE, true, null, false, 1);
    }
}
