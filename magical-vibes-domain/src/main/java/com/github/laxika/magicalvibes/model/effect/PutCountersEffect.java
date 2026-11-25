package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/**
 * Puts counter(s) of the specified type on the permanents {@code subject} names: by default the targeted permanent
 * ("put a +1/+1 counter on target creature"), or a permanent named without targeting it:
 * {@link Subjects#source()} ("put a +1/+1 counter on this creature": {@link #onSelf(CounterType)} and its overloads),
 * {@link Subjects#triggering()} ("put a +1/+1 counter on it", the permanent the trigger event was about),
 * {@link Subjects#attached()} ("put a +1/+1 counter on enchanted creature", "on equipped creature"),
 * {@link Subjects#returned()} ("put a +1/+1 counter on that creature" after returning it to the battlefield) or
 * {@link Subjects#granter()} ("put an aim counter on this Equipment" in an ability the Equipment grants).
 *
 * <p>When {@code predicate} is {@code null} and the subject is {@code target()}, this is a targeting effect — the
 * permanent is chosen as a target when the spell or ability is cast. For lore counters on Sagas, this also
 * triggers the appropriate chapter ability per CR 715.2b. In that mode, {@code targetPredicate}
 * may further restrict which permanents are legal targets (honoured by the saga-chapter and
 * end-step targeting pipelines).</p>
 *
 * <p>When {@code predicate} is non-null, this is a resolution-time choice — the controller
 * chooses a permanent they control matching the predicate during resolution. If no permanents
 * match, the effect does nothing. If exactly one matches, it is automatically chosen.
 * If multiple match, the controller chooses one.</p>
 *
 * <p>{@code amount} is the number of counters (a {@link DynamicAmount}: {@link Fixed} for a flat
 * count, {@code XValue()} for "X +1/+1 counters", …). For a target it is evaluated against the ability's source; for
 * a non-target subject against the named permanent ("equal to its power", "for each creature blocking it").
 * {@code regenerateIfSurvives} regenerates the target after placing counters when its toughness stays ≥ 1 (Gore
 * Vassal).</p>
 *
 * <p>{@code resolutionCondition}, when non-null, gates counter placement at resolution: the
 * counters are only placed if the permanent matches the predicate then ("if it's legendary" —
 * Ancient Animus; "if it's blue" — the M13 Rings). Unlike {@code targetPredicate}, this does not restrict target
 * legality.</p>
 *
 * <p>A non-target subject declares no target, so shroud and hexproof don't stop it and a permanent that is gone at
 * resolution simply gets nothing. It takes no {@code predicate}, {@code targetPredicate},
 * {@code regenerateIfSurvives} or {@code optionalTarget}.</p>
 *
 * <p>The effect's controller puts the counters, unless {@code eachControllerPlaces}: then each
 * permanent's controller puts them on it ({@link #placedByEachController()}: "target player puts a +1/+1 counter on
 * each creature they control", "each opponent may put a +1/+1 counter on each creature they control"), so the
 * replacements and triggers that ask who puts them ("if you would put", "if an opponent would put") see that
 * player.</p>
 *
 * <p>On {@link Subjects#source()} the effect keeps the self-targeting marker of an ability about its own source
 * (an activated ability's target is its source), unless the ability declares a target the counters don't go on:
 * {@code targetsPlayer} (a target player the amount counts for, {@link #onSelfCountingTargetPlayer}) or
 * {@code declaresPermanentTarget} (a target permanent another part of the ability reads,
 * {@link #onSelfWithTargetPermanent}). On a planar ability the source is the face-up plane or phenomenon.</p>
 */
public record PutCountersEffect(CounterType counterType, DynamicAmount amount,
                                                PermanentPredicate predicate,
                                                PermanentPredicate targetPredicate,
                                                boolean regenerateIfSurvives,
                                                PermanentPredicate resolutionCondition,
                                                boolean optionalTarget,
                                                PermanentSubject subject,
                                                boolean eachControllerPlaces,
                                                boolean targetsPlayer,
                                                boolean declaresPermanentTarget)
        implements CardEffect {

    public PutCountersEffect {
        if (subject == null) {
            subject = Subjects.target();
        }
        if ((targetsPlayer || declaresPermanentTarget) && !(subject instanceof PermanentSubject.Source)) {
            throw new IllegalArgumentException("Only counters on the source declare a target they don't go on: "
                    + subject);
        }
        if (!(subject instanceof PermanentSubject.Target)
                && (predicate != null || targetPredicate != null || regenerateIfSurvives || optionalTarget)) {
            throw new IllegalArgumentException("A resolution-time choice, a target restriction, regeneration and an "
                    + "optional target need the target subject: " + subject);
        }
    }

    /** A counter effect that declares no target besides its subject's. */
    public PutCountersEffect(CounterType counterType, DynamicAmount amount,
                             PermanentPredicate predicate,
                             PermanentPredicate targetPredicate,
                             boolean regenerateIfSurvives,
                             PermanentPredicate resolutionCondition,
                             boolean optionalTarget,
                             PermanentSubject subject,
                             boolean eachControllerPlaces) {
        this(counterType, amount, predicate, targetPredicate, regenerateIfSurvives, resolutionCondition,
                optionalTarget, subject, eachControllerPlaces, false, false);
    }

    /** A counter effect whose counters the effect's controller puts. */
    public PutCountersEffect(CounterType counterType, DynamicAmount amount,
                                             PermanentPredicate predicate,
                                             PermanentPredicate targetPredicate,
                                             boolean regenerateIfSurvives,
                                             PermanentPredicate resolutionCondition,
                                             boolean optionalTarget,
                                             PermanentSubject subject) {
        this(counterType, amount, predicate, targetPredicate, regenerateIfSurvives, resolutionCondition,
                optionalTarget, subject, false);
    }

    /** The full constructor of a target or resolution-time-choice counter effect. */
    public PutCountersEffect(CounterType counterType, DynamicAmount amount,
                                             PermanentPredicate predicate,
                                             PermanentPredicate targetPredicate,
                                             boolean regenerateIfSurvives,
                                             PermanentPredicate resolutionCondition,
                                             boolean optionalTarget) {
        this(counterType, amount, predicate, targetPredicate, regenerateIfSurvives, resolutionCondition,
                optionalTarget, null);
    }

    /** One counter of {@code counterType} on the permanent {@code subject} names. */
    public PutCountersEffect(PermanentSubject subject, CounterType counterType) {
        this(subject, counterType, new Fixed(1));
    }

    /** {@code count} counters of {@code counterType} on the permanent {@code subject} names. */
    public PutCountersEffect(PermanentSubject subject, CounterType counterType, int count) {
        this(subject, counterType, new Fixed(count));
    }

    /** {@code amount} counters of {@code counterType} on the permanent {@code subject} names. */
    public PutCountersEffect(PermanentSubject subject, CounterType counterType, DynamicAmount amount) {
        this(subject, counterType, amount, null);
    }

    /**
     * {@code amount} counters of {@code counterType} on the permanent {@code subject} names, only if it matches
     * {@code condition} as the effect resolves ("put a +1/+1 counter on equipped creature if it's blue").
     */
    public PutCountersEffect(PermanentSubject subject, CounterType counterType, DynamicAmount amount,
                                             PermanentPredicate condition) {
        this(counterType, amount, null, null, false, condition, false, subject);
    }

    public PutCountersEffect(CounterType counterType) {
        this(counterType, new Fixed(1), null, null, false, null, false);
    }

    public PutCountersEffect(CounterType counterType, int count) {
        this(counterType, new Fixed(count), null, null, false, null, false);
    }

    public PutCountersEffect(CounterType counterType, DynamicAmount amount) {
        this(counterType, amount, null, null, false, null, false);
    }

    /** Backward-compatible full constructor for a mandatory targeted counter effect. */
    public PutCountersEffect(CounterType counterType, DynamicAmount amount,
                                              PermanentPredicate predicate,
                                              PermanentPredicate targetPredicate,
                                              boolean regenerateIfSurvives,
                                              PermanentPredicate resolutionCondition) {
        this(counterType, amount, predicate, targetPredicate, regenerateIfSurvives,
                resolutionCondition, false);
    }

    public PutCountersEffect(CounterType counterType, int count, PermanentPredicate predicate) {
        this(counterType, new Fixed(count), predicate, null, false, null, false);
    }

    public PutCountersEffect(CounterType counterType, int count, boolean regenerateIfSurvives) {
        this(counterType, new Fixed(count), null, null, regenerateIfSurvives, null, false);
    }

    /** "Put a [kind] counter on this [permanent]": one counter on the ability's source. */
    public static PutCountersEffect onSelf(CounterType counterType) {
        return onSelf(counterType, new Fixed(1));
    }

    /** "Put N [kind] counters on this [permanent]". */
    public static PutCountersEffect onSelf(CounterType counterType, int count) {
        return onSelf(counterType, new Fixed(count));
    }

    /**
     * "Put X [kind] counters on this [permanent]", {@code amount} evaluated as it resolves against the source
     * ({@code XValue()} for "{X}: put X tower counters on this").
     */
    public static PutCountersEffect onSelf(CounterType counterType, DynamicAmount amount) {
        return new PutCountersEffect(Subjects.source(), counterType, amount);
    }

    /**
     * {@link #onSelf(CounterType, DynamicAmount)} in an ability that targets a player the amount counts for ("put a
     * loyalty counter on this for each card in target opponent's hand").
     */
    public static PutCountersEffect onSelfCountingTargetPlayer(CounterType counterType, DynamicAmount amount) {
        return new PutCountersEffect(counterType, amount, null, null, false, null, false, Subjects.source(), false,
                true, false);
    }

    /**
     * {@link #onSelf(CounterType)} in an ability that targets a permanent another part of it reads ("if target
     * [permanent] has [quality], put a counter on this").
     */
    public static PutCountersEffect onSelfWithTargetPermanent(CounterType counterType) {
        return new PutCountersEffect(counterType, new Fixed(1), null, null, false, null, false, Subjects.source(),
                false, false, true);
    }

    /** Targeting effect for "put counters on up to one target permanent" triggered abilities. */
    public static PutCountersEffect upToOneTarget(CounterType counterType,
                                                                    DynamicAmount amount) {
        return new PutCountersEffect(counterType, amount, null, null, false, null, true);
    }

    /**
     * "Put N counters on it / that creature", "it" the stack entry's triggering permanent (the creature that
     * entered): not targeted, and placed wherever that permanent is as the ability resolves. Its controller orders
     * the counter replacements as for a target.
     */
    public static PutCountersEffect onTriggeringPermanent(CounterType counterType,
                                                                          DynamicAmount amount) {
        return new PutCountersEffect(Subjects.triggering(), counterType, amount);
    }

    /** {@link #onTriggeringPermanent(CounterType, DynamicAmount)} with a fixed count. */
    public static PutCountersEffect onTriggeringPermanent(CounterType counterType, int count) {
        return onTriggeringPermanent(counterType, new Fixed(count));
    }

    /** Targeting effect whose legal targets are restricted to permanents matching {@code targetPredicate}. */
    public static PutCountersEffect withTargetRestriction(CounterType counterType, int count,
                                                                          PermanentPredicate targetPredicate) {
        return new PutCountersEffect(counterType, new Fixed(count), null, targetPredicate, false, null, false);
    }

    /** As {@link #withTargetRestriction(CounterType, int, PermanentPredicate)}, with a count evaluated as it resolves. */
    public static PutCountersEffect withTargetRestriction(CounterType counterType, DynamicAmount amount,
                                                                          PermanentPredicate targetPredicate) {
        return new PutCountersEffect(counterType, amount, null, targetPredicate, false, null, false);
    }

    /**
     * Targeting effect that only places counters when the target matches {@code resolutionCondition}
     * at resolution ("put a +1/+1 counter on target creature you control if it's legendary" — Ancient
     * Animus). A non-matching permanent is still a legal target; the counters are simply not placed.
     */
    public static PutCountersEffect withResolutionCondition(CounterType counterType, int count,
                                                                            PermanentPredicate resolutionCondition) {
        return new PutCountersEffect(counterType, new Fixed(count), null, null, false, resolutionCondition, false);
    }

    /** This effect on the permanent {@code subject} names, everything else kept. */
    public PutCountersEffect withSubject(PermanentSubject subject) {
        return new PutCountersEffect(counterType, amount, predicate, targetPredicate,
                regenerateIfSurvives, resolutionCondition, optionalTarget, subject, eachControllerPlaces,
                targetsPlayer, declaresPermanentTarget);
    }

    /**
     * This effect with each permanent's controller putting the counters on it ("target player puts a +1/+1 counter on
     * each creature they control"), for a non-target subject.
     */
    public PutCountersEffect placedByEachController() {
        if (subject instanceof PermanentSubject.Target) {
            throw new IllegalArgumentException("Counters put by each controller name their permanents without a target");
        }
        return new PutCountersEffect(counterType, amount, predicate, targetPredicate,
                regenerateIfSurvives, resolutionCondition, optionalTarget, subject, true, targetsPlayer,
                declaresPermanentTarget);
    }

    @Override
    public TargetSpec targetSpec() {
        if (subject instanceof PermanentSubject.Source) {
            if (targetsPlayer) {
                return TargetSpec.benign(TargetPredicates.player());
            }
            return declaresPermanentTarget ? TargetSpec.benign(TargetPredicates.permanent())
                    : new TargetSpec(null, false, null, true, 1);
        }
        return predicate == null && subject.declaresTarget()
                ? TargetSpec.benign(TargetPredicates.permanent(), targetPredicate) : TargetSpec.NONE;
    }

    /**
     * Binds the enchanted or equipped permanent as an Aura's or Equipment's activation is paid for, so a cost that
     * detaches the source (Krasis Incubation's "return this Aura to its owner's hand") does not lose it.
     */
    @Override
    public boolean resolvesAgainstAttachedPermanent() {
        return subject instanceof PermanentSubject.Attached;
    }

    @Override
    public boolean hasOptionalTarget() {
        return optionalTarget;
    }
}
