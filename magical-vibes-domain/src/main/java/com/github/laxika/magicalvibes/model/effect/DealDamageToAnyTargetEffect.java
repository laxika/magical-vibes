package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.condition.Condition;
import com.github.laxika.magicalvibes.model.filter.PermanentIsSourcePermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.AnyTargetPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilter;

import java.util.List;

/**
 * Deals damage to any target (creature, planeswalker, or player). The amount is a
 * {@link DynamicAmount} evaluated at resolution (fixed number, X paid, source's power,
 * counters on the source, …).
 *
 * @param damage             the amount of damage to deal
 * @param cantRegenerate     when true, a creature dealt damage this way can't regenerate
 *                           this turn (e.g. Incinerate)
 * @param exileInsteadOfDie  when true, a creature dealt damage this way that would die
 *                           this turn is exiled instead (replacement effect, CR 614.6 —
 *                           e.g. Red Sun's Zenith)
 * @param targetGroup        which target the damage applies to: {@code -1} (default) resolves
 *                           against the entry's single target ({@code targetId}); {@code >= 0}
 *                           resolves against that target group's chosen targets (see
 *                           {@code StackEntry.targetsForGroup} — for cards without
 *                           {@code target(...)} declarations the index addresses the flat
 *                           additional-target list, e.g. Goblin Barrage's kicked target)
 * @param unpreventableWhen  when non-null and met at resolution (evaluated against the stack
 *                           entry), this damage can't be prevented — e.g. Banefire's "If X is
 *                           5 or more, … the damage can't be prevented" uses
 *                           {@code new SpellXAtLeast(5)}. {@code null} = normal prevention.
 * @param cantBeRedirectedWhenUnpreventable when true, an event made unpreventable by
 *                           {@code unpreventableWhen} also can't be redirected
 * @param preventRegenerationWithoutDamage when true, the targeted creature cannot regenerate
 *                           even if no damage is dealt (Disintegrate)
 * @param sourceIsTriggeringPermanent whether an enter trigger binds the entering permanent as damage source
 */
public record DealDamageToAnyTargetEffect(DynamicAmount damage, boolean cantRegenerate,
                                          boolean exileInsteadOfDie, int targetGroup,
                                          Condition unpreventableWhen,
                                          boolean cantBeRedirectedWhenUnpreventable,
                                          boolean preventRegenerationWithoutDamage,
                                          boolean sourceIsTriggeringPermanent,
                                          boolean recordDamageDealt)
        implements DamageDealingEffect {

    public DealDamageToAnyTargetEffect(DynamicAmount damage, boolean cantRegenerate,
                                       boolean exileInsteadOfDie, int targetGroup,
                                       Condition unpreventableWhen,
                                       boolean cantBeRedirectedWhenUnpreventable,
                                       boolean preventRegenerationWithoutDamage,
                                       boolean sourceIsTriggeringPermanent) {
        this(damage, cantRegenerate, exileInsteadOfDie, targetGroup, unpreventableWhen,
                cantBeRedirectedWhenUnpreventable, preventRegenerationWithoutDamage, sourceIsTriggeringPermanent, false);
    }

    /** Returns the same effect with a different damage amount (for example one fixed at trigger time). */
    public DealDamageToAnyTargetEffect withDamage(DynamicAmount newDamage) {
        return new DealDamageToAnyTargetEffect(newDamage, cantRegenerate, exileInsteadOfDie, targetGroup,
                unpreventableWhen, cantBeRedirectedWhenUnpreventable,
                preventRegenerationWithoutDamage, sourceIsTriggeringPermanent, recordDamageDealt);
    }

    /** Stores the damage actually dealt in the stack entry for subsequent effects. */
    public DealDamageToAnyTargetEffect recordingDamageDealt() {
        return new DealDamageToAnyTargetEffect(damage, cantRegenerate, exileInsteadOfDie, targetGroup,
                unpreventableWhen, cantBeRedirectedWhenUnpreventable,
                preventRegenerationWithoutDamage, sourceIsTriggeringPermanent, true);
    }

    public DealDamageToAnyTargetEffect(DynamicAmount damage, boolean cantRegenerate,
                                       boolean exileInsteadOfDie, int targetGroup,
                                       Condition unpreventableWhen,
                                       boolean cantBeRedirectedWhenUnpreventable,
                                       boolean preventRegenerationWithoutDamage) {
        this(damage, cantRegenerate, exileInsteadOfDie, targetGroup, unpreventableWhen,
                cantBeRedirectedWhenUnpreventable, preventRegenerationWithoutDamage, false);
    }

    /**
     * The permanent the trigger event was about (the stack entry's triggering permanent: the creature that entered, was
     * dealt damage, …) deals the damage, with its live characteristics or last known values (CR 608.2h): "it deals
     * damage equal to its power to any target".
     */
    public static DealDamageToAnyTargetEffect dealtByTriggeringPermanent(DynamicAmount damage) {
        return new DealDamageToAnyTargetEffect(damage, false, false, -1, null, false, false, true);
    }

    private static final int ANY_OTHER_TARGET = -2;
    private static final int EVENT_OBJECT = -3;

    public DealDamageToAnyTargetEffect(DynamicAmount damage, boolean cantRegenerate,
                                       boolean exileInsteadOfDie, int targetGroup,
                                       Condition unpreventableWhen,
                                       boolean cantBeRedirectedWhenUnpreventable) {
        this(damage, cantRegenerate, exileInsteadOfDie, targetGroup, unpreventableWhen,
                cantBeRedirectedWhenUnpreventable, false);
    }

    /** Applies the no-regeneration rider to the targeted creature regardless of damage dealt. */
    public DealDamageToAnyTargetEffect withUnconditionalRegenerationPrevention() {
        return new DealDamageToAnyTargetEffect(damage, true, exileInsteadOfDie, targetGroup,
                unpreventableWhen, cantBeRedirectedWhenUnpreventable, true,
                sourceIsTriggeringPermanent, recordDamageDealt);
    }

    public DealDamageToAnyTargetEffect(DynamicAmount damage, boolean cantRegenerate,
                                       boolean exileInsteadOfDie, int targetGroup,
                                       Condition unpreventableWhen) {
        this(damage, cantRegenerate, exileInsteadOfDie, targetGroup, unpreventableWhen, false);
    }

    public DealDamageToAnyTargetEffect(DynamicAmount damage, boolean cantRegenerate, boolean exileInsteadOfDie, int targetGroup) {
        this(damage, cantRegenerate, exileInsteadOfDie, targetGroup, null);
    }

    public DealDamageToAnyTargetEffect(DynamicAmount damage, boolean cantRegenerate, boolean exileInsteadOfDie) {
        this(damage, cantRegenerate, exileInsteadOfDie, -1, null);
    }

    public DealDamageToAnyTargetEffect(int damage) {
        this(new Fixed(damage), false, false, -1, null);
    }

    public DealDamageToAnyTargetEffect(int damage, boolean cantRegenerate) {
        this(new Fixed(damage), cantRegenerate, false, -1, null);
    }

    public DealDamageToAnyTargetEffect(DynamicAmount damage) {
        this(damage, false, false, -1, null);
    }

    /** "Deals damage to any target; that damage can't be prevented while {@code unpreventableWhen} holds" (Banefire). */
    public DealDamageToAnyTargetEffect(DynamicAmount damage, Condition unpreventableWhen) {
        this(damage, false, false, -1, unpreventableWhen);
    }

    /** Damage that also can't be redirected while {@code unpreventableWhen} holds. */
    public DealDamageToAnyTargetEffect(DynamicAmount damage, Condition unpreventableWhen,
                                       boolean cantBeRedirectedWhenUnpreventable) {
        this(damage, false, false, -1, unpreventableWhen,
                cantBeRedirectedWhenUnpreventable);
    }

    /** Damage aimed at the given target group's chosen target instead of the entry's single target. */
    public static DealDamageToAnyTargetEffect forTargetGroup(int damage, int targetGroup) {
        return new DealDamageToAnyTargetEffect(new Fixed(damage), false, false, targetGroup, null);
    }

    /** Damage to any target other than the effect's source permanent. */
    public static DealDamageToAnyTargetEffect toAnyOtherTarget(DynamicAmount damage) {
        return new DealDamageToAnyTargetEffect(damage, false, false, ANY_OTHER_TARGET, null);
    }

    /**
     * Damage to the permanent or player the trigger event names ("deals 2 damage to that permanent or player", Ghyrson
     * Starn), the entry's non-targeting target (CR 115.10a): the ability targets nothing.
     */
    public static DealDamageToAnyTargetEffect toEventObject(DynamicAmount damage) {
        return new DealDamageToAnyTargetEffect(damage, false, false, EVENT_OBJECT, null);
    }

    /** Target filter for an any-target effect that excludes its source permanent. */
    public static TargetFilter anyOtherTargetFilter() {
        return new AnyTargetPredicateTargetFilter(
                new PermanentAllOfPredicate(List.of(
                        TargetPredicates.anyTarget().permanentRestriction().orElseThrow(),
                        new PermanentNotPredicate(new PermanentIsSourcePermanentPredicate()))),
                new PlayerRelationPredicate(PlayerRelation.ANY),
                "Target must be another target");
    }

    @Override
    public TargetSpec targetSpec() {
        if (targetGroup == EVENT_OBJECT) {
            return TargetSpec.NONE;
        }
        return targetGroup == ANY_OTHER_TARGET
                ? TargetSpec.harmful(TargetPredicates.anyTarget(),
                        new PermanentNotPredicate(new PermanentIsSourcePermanentPredicate()))
                : TargetSpec.harmful(TargetPredicates.anyTarget());
    }

    @Override
    public TargetFilter triggeredTargetFilter() {
        return targetGroup == ANY_OTHER_TARGET ? anyOtherTargetFilter() : null;
    }

    @Override
    public DynamicAmount damageAmount() {
        return damage;
    }

    @Override
    public boolean canDamageCreatures() {
        return true;
    }

    @Override
    public boolean canDamagePlayers() {
        return true;
    }

}
