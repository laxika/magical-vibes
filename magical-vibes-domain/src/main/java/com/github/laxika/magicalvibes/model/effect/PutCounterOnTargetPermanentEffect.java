package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/**
 * Puts counter(s) of the specified type on a permanent.
 *
 * <p>When {@code predicate} is {@code null}, this is a targeting effect — the permanent is
 * chosen as a target when the spell or ability is cast. For lore counters on Sagas, this also
 * triggers the appropriate chapter ability per MTG Rule 714.3b. In that mode, {@code targetPredicate}
 * may further restrict which permanents are legal targets (honoured by the saga-chapter and
 * end-step targeting pipelines).</p>
 *
 * <p>When {@code predicate} is non-null, this is a resolution-time choice — the controller
 * chooses a permanent they control matching the predicate during resolution. If no permanents
 * match, the effect does nothing. If exactly one matches, it is automatically chosen.
 * If multiple match, the controller chooses one.</p>
 *
 * <p>{@code amount} is the number of counters (a {@link DynamicAmount}: {@link Fixed} for a flat
 * count, {@code XValue()} for "X +1/+1 counters", …). {@code regenerateIfSurvives} regenerates the
 * target after placing counters when its toughness stays ≥ 1 (Gore Vassal).</p>
 *
 * <p>{@code resolutionCondition}, when non-null, gates counter placement at resolution: the
 * counters are only placed if the target matches the predicate then ("if it's legendary" —
 * Ancient Animus). Unlike {@code targetPredicate}, this does not restrict target legality.</p>
 */
public record PutCounterOnTargetPermanentEffect(CounterType counterType, DynamicAmount amount,
                                                PermanentPredicate predicate,
                                                PermanentPredicate targetPredicate,
                                                boolean regenerateIfSurvives,
                                                PermanentPredicate resolutionCondition) implements CardEffect {

    public PutCounterOnTargetPermanentEffect(CounterType counterType) {
        this(counterType, new Fixed(1), null, null, false, null);
    }

    public PutCounterOnTargetPermanentEffect(CounterType counterType, int count) {
        this(counterType, new Fixed(count), null, null, false, null);
    }

    public PutCounterOnTargetPermanentEffect(CounterType counterType, DynamicAmount amount) {
        this(counterType, amount, null, null, false, null);
    }

    public PutCounterOnTargetPermanentEffect(CounterType counterType, int count, PermanentPredicate predicate) {
        this(counterType, new Fixed(count), predicate, null, false, null);
    }

    public PutCounterOnTargetPermanentEffect(CounterType counterType, int count, boolean regenerateIfSurvives) {
        this(counterType, new Fixed(count), null, null, regenerateIfSurvives, null);
    }

    /** Targeting effect whose legal targets are restricted to permanents matching {@code targetPredicate}. */
    public static PutCounterOnTargetPermanentEffect withTargetRestriction(CounterType counterType, int count,
                                                                          PermanentPredicate targetPredicate) {
        return new PutCounterOnTargetPermanentEffect(counterType, new Fixed(count), null, targetPredicate, false, null);
    }

    /**
     * Targeting effect that only places counters when the target matches {@code resolutionCondition}
     * at resolution ("put a +1/+1 counter on target creature you control if it's legendary" — Ancient
     * Animus). A non-matching permanent is still a legal target; the counters are simply not placed.
     */
    public static PutCounterOnTargetPermanentEffect withResolutionCondition(CounterType counterType, int count,
                                                                            PermanentPredicate resolutionCondition) {
        return new PutCounterOnTargetPermanentEffect(counterType, new Fixed(count), null, null, false, resolutionCondition);
    }

    @Override
    public boolean canTargetPermanent() { return predicate == null; }
}
