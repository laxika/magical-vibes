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
 *
 * <p>{@code damageSource} names who deals it: the ability's source, or with
 * {@link DamageSource#TRIGGERING_PERMANENT} the permanent the trigger event was about ("sacrifice it. If you do, it deals
 * 4 damage to each creature blocking it", Ib Halfheart), as it is now or as it last existed (CR 608.2h); the predicate
 * is then judged with that permanent as the source.</p>
 */
public record DealDamageToEachMatchingPermanentEffect(DynamicAmount damage, PermanentPredicate predicate,
                                                      EachPermanentScope scope, boolean onlyDeclaredAttackers,
                                                      DamageSource damageSource)
        implements CardEffect {

    public DealDamageToEachMatchingPermanentEffect {
        damageSource = damageSource == null ? DamageSource.ABILITY_SOURCE : damageSource;
        if (damageSource == DamageSource.EVENT_DAMAGE_SOURCE) {
            throw new IllegalArgumentException("The event's damage source deals no damage to each matching permanent");
        }
    }

    public DealDamageToEachMatchingPermanentEffect(DynamicAmount damage, PermanentPredicate predicate,
                                                   EachPermanentScope scope, boolean onlyDeclaredAttackers) {
        this(damage, predicate, scope, onlyDeclaredAttackers, DamageSource.ABILITY_SOURCE);
    }

    /** This effect's damage dealt by the permanent the trigger event was about. */
    public DealDamageToEachMatchingPermanentEffect dealtByTriggeringPermanent() {
        return new DealDamageToEachMatchingPermanentEffect(damage, predicate, scope, onlyDeclaredAttackers,
                DamageSource.TRIGGERING_PERMANENT);
    }

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
