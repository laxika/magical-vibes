package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.amount.XValue;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/**
 * Target creature gets +X/+Y until end of turn. The amounts are {@link DynamicAmount}s, so
 * "gets +3/+3", "gets +X/+X" (X paid), and "gets +1/+1 for each creature you control" are the
 * same effect with different amount parameters. Counting contexts ("you control", "in your
 * graveyard") resolve against the effect's controller, not the target.
 *
 * <p>{@code filter} is an OPTIONAL targeting restriction ({@code null} for a plain "target
 * creature"). When set it narrows the {@link #targetSpec()} predicate — e.g. Ominous Sphinx's
 * "target creature an opponent controls gets -2/-0" passes a "creature an opponent controls"
 * predicate. Trigger pipelines that read {@code targetSpec().predicate()} (such as the
 * cycle/discard controller-trigger target collector) use it to build the legal-target list.
 *
 * <p>{@code targetGroup} is an activated-ability target-group index, or {@code -1} when the
 * effect uses the card's target binding or the entry's ordinary target list.
 *
 * <p>{@code duration} is {@link Duration#UNTIL_END_OF_TURN} for the overwhelming majority of pumps;
 * {@link Duration#UNTIL_YOUR_NEXT_TURN} lasts until its controller's next turn (Jace, Telepath Unbound's +1),
 * {@link Duration#UNTIL_END_OF_COMBAT} until end of combat, and {@link Duration#PERMANENT} is a boost with no stated
 * duration, which lasts indefinitely (CR 611.2a; Riding the Dilu Horse).
 *
 * <p>On a block trigger whose card declares no target for it ("whenever this creature blocks …,
 * that creature gets +1/+1") the boosted creature is the creature on the other side of the block,
 * which the collector records as the trigger's non-targeting target.
 *
 * <p>{@code subject} is {@link Subjects#target()} for the plain pump. A subject that names the target together with
 * other creatures boosts every creature it names as the effect resolves, the set fixed then (CR 611.2c): "target
 * creature and all other creatures with the same name as that creature get +2/+2" is
 * {@code allOf(target(), otherCreaturesSharing(target(), NAME))}, "target creature and each other creature that shares
 * a color with it" is {@code withCreaturesSharingAColor(target())}. Only the target is targeted. "That creature gets
 * +7/+0" after a token is created is {@link Subjects#createdThisResolution()} (Elemental Appeal).
 */
public record BoostTargetCreatureEffect(DynamicAmount powerBoost, DynamicAmount toughnessBoost,
                                        PermanentPredicate filter,
                                        Duration duration,
                                        int targetGroup,
                                        DynamicAmount castTimeXValue,
                                        PermanentSubject subject)
        implements CreatureBoostEffect, CastTimeXValueEffect {

    private static final int UNBOUND_TARGET_GROUP = -1;

    public BoostTargetCreatureEffect {
        subject = subject == null ? Subjects.target() : subject;
        if (!subject.declaresTarget() && !(subject instanceof PermanentSubject.ThisWay)
                && !(subject instanceof PermanentSubject.Except)
                && !(subject instanceof PermanentSubject.CreatedThisResolution)
                && !(subject instanceof PermanentSubject.AttachedAndSharingCreatureType)) {
            throw new IllegalArgumentException("BoostTargetCreatureEffect boosts its target or what a choice named, "
                    + "left or created; another non-targeting subject is BoostSelfEffect: " + subject);
        }
    }

    public BoostTargetCreatureEffect(DynamicAmount powerBoost, DynamicAmount toughnessBoost,
                                     PermanentPredicate filter, Duration duration, int targetGroup,
                                     DynamicAmount castTimeXValue) {
        this(powerBoost, toughnessBoost, filter, duration, targetGroup, castTimeXValue, Subjects.target());
    }

    /** "Each of them gets +X/+Y until end of turn": X and Y found once, as the effect is applied (CR 608.2h). */
    public BoostTargetCreatureEffect(PermanentSubject subject, DynamicAmount powerBoost, DynamicAmount toughnessBoost) {
        this(powerBoost, toughnessBoost, null, Duration.UNTIL_END_OF_TURN, UNBOUND_TARGET_GROUP, null, subject);
    }

    /** "Target creature and all other creatures with the same name as that creature get +X/+Y until end of turn". */
    public BoostTargetCreatureEffect(PermanentSubject subject, int powerBoost, int toughnessBoost) {
        this(new Fixed(powerBoost), new Fixed(toughnessBoost), null, Duration.UNTIL_END_OF_TURN,
                UNBOUND_TARGET_GROUP, null, subject);
    }

    public BoostTargetCreatureEffect(DynamicAmount powerBoost, DynamicAmount toughnessBoost,
                                     PermanentPredicate filter, Duration duration) {
        this(powerBoost, toughnessBoost, filter, duration, UNBOUND_TARGET_GROUP, null);
    }

    public BoostTargetCreatureEffect(DynamicAmount powerBoost, DynamicAmount toughnessBoost,
                                     PermanentPredicate filter) {
        this(powerBoost, toughnessBoost, filter, Duration.UNTIL_END_OF_TURN);
    }

    public BoostTargetCreatureEffect(DynamicAmount powerBoost, DynamicAmount toughnessBoost) {
        this(powerBoost, toughnessBoost, null);
    }

    public BoostTargetCreatureEffect(DynamicAmount powerBoost, DynamicAmount toughnessBoost, int targetGroup) {
        this(powerBoost, toughnessBoost, null, Duration.UNTIL_END_OF_TURN, targetGroup, null);
    }

    /** Convenience for plain fixed boosts ("gets +2/+2 until end of turn"). */
    public BoostTargetCreatureEffect(int powerBoost, int toughnessBoost) {
        this(new Fixed(powerBoost), new Fixed(toughnessBoost), null);
    }

    /** Convenience for fixed boosts narrowed by a target restriction ("target creature an opponent controls gets -2/-0"). */
    public BoostTargetCreatureEffect(int powerBoost, int toughnessBoost, PermanentPredicate filter) {
        this(new Fixed(powerBoost), new Fixed(toughnessBoost), filter);
    }

    /** Convenience for fixed boosts with a non-default duration ("gets -2/-0 until your next turn"). */
    public BoostTargetCreatureEffect(int powerBoost, int toughnessBoost, Duration duration) {
        this(new Fixed(powerBoost), new Fixed(toughnessBoost), null, duration);
    }

    /** Convenience for an activated ability's positional target group. */
    public static BoostTargetCreatureEffect forTargetGroup(int powerBoost, int toughnessBoost, int targetGroup) {
        return new BoostTargetCreatureEffect(new Fixed(powerBoost), new Fixed(toughnessBoost), null,
                Duration.UNTIL_END_OF_TURN, targetGroup, null);
    }

    /**
     * A boost whose X value is evaluated while the spell is cast and made available to the boost
     * amounts through {@link XValue}.
     */
    public static BoostTargetCreatureEffect withCastTimeXValue(
            DynamicAmount castTimeXValue, DynamicAmount powerBoost, DynamicAmount toughnessBoost) {
        return new BoostTargetCreatureEffect(powerBoost, toughnessBoost, null,
                Duration.UNTIL_END_OF_TURN, UNBOUND_TARGET_GROUP, castTimeXValue);
    }

    @Override
    public DynamicAmount castTimeXValue() {
        return castTimeXValue;
    }

    @Override
    public boolean resolvesAgainstAttachedPermanent() {
        return subject instanceof PermanentSubject.AttachedAndSharingCreatureType;
    }

    @Override
    public TargetSpec targetSpec() {
        return subject.declaresTarget() ? TargetSpec.benign(TargetPredicates.creature(), filter) : TargetSpec.NONE;
    }
}
