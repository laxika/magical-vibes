package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.amount.Scaled;
import com.github.laxika.magicalvibes.model.amount.XValue;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

import java.util.Set;

/**
 * "[Permanent] gets +X/+Y [and gains K] [for a duration]": a one-shot layer-7c floating effect on each permanent the
 * {@code subject} names (CR 613.4c), lasting {@code duration} (CR 611.2a: {@link Duration#PERMANENT} for no stated
 * duration). Built through the factories, which read like the Oracle wording:
 *
 * <ul>
 *   <li>{@link #self} — "this creature gets +X/+Y until end of turn" ({@link Subjects#source()}); in the
 *       {@code STATIC} slot, without keywords, "this creature gets +1/+1 for each …" is a continuous self bonus,
 *       re-read at every layered pass (CR 611.3a);</li>
 *   <li>{@link #of} — one named, non-targeted permanent: "equipped creature gets", "it gets +1/+1 for each creature
 *       blocking it" ({@link Subjects#attached()}, {@link Subjects#triggering()}, …), optionally gaining keywords;</li>
 *   <li>{@link #target} / {@link #targetInGroup} / {@link #withCastTimeXValue} — "target creature gets", the
 *       optional {@code filter} narrowing the target; {@link #targetPermanent} — "target creature or Vehicle", a
 *       permanent target (CR 301.7a, 208.3);</li>
 *   <li>{@link #each} — the target and the creatures named with it, or what an earlier step named ("target creature
 *       and all other creatures with the same name", "that creature gets +7/+0" after a token step): only creatures,
 *       the set found once as the effect resolves (CR 611.2c);</li>
 *   <li>{@link #creaturesYouControl} / {@link #allCreatures} / {@link #creaturesTargetPlayerControls} — "creatures you
 *       control get", "all creatures get", "creatures target player controls get", the optional filter narrowing the
 *       set ({@link Subjects#narrowed}); the set is found once as the effect resolves (CR 611.2c), and "those
 *       creatures get" after an attack trigger is {@link #each} over {@link Subjects#triggeringAttackers()}.</li>
 * </ul>
 *
 * <p>{@code amountsRead} says which permanent the amounts read as their source (CR 608.2h: found once, as the
 * effect is applied): {@link AmountsRead#BOOSTED} the boosted permanent ({@code self} and {@code of}: "double its
 * power"), {@link AmountsRead#SOURCE} the effect's source, live or as it last existed (every target and set form:
 * "gets +X/+X where X is this creature's power"). The factory sets it; the subject can't tell, since
 * {@link Subjects#thisWay} is used both ways.
 *
 * <p>{@code targetGroup} is an activated ability's positional target group, or {@code -1}; {@code castTimeXValue}
 * is evaluated as the spell is cast and read through {@link XValue}.
 */
public record BoostEffect(DynamicAmount powerBoost, DynamicAmount toughnessBoost, PermanentSubject subject,
                          AmountsRead amountsRead, Duration duration, Set<Keyword> keywords,
                          PermanentPredicate filter, boolean targetsPermanent, int targetGroup,
                          DynamicAmount castTimeXValue)
        implements CreatureBoostEffect, CastTimeXValueEffect {

    /** Which permanent a boost's amounts read as their source. */
    public enum AmountsRead {
        /** The effect's source, live or as it last existed (CR 608.2h). */
        SOURCE,
        /** The one permanent being boosted. */
        BOOSTED
    }

    private static final int UNBOUND_TARGET_GROUP = -1;

    public BoostEffect {
        if (subject == null || amountsRead == null) {
            throw new IllegalArgumentException("BoostEffect needs a subject and an amount reference");
        }
        duration = duration == null ? Duration.UNTIL_END_OF_TURN : duration;
        keywords = keywords == null ? Set.of() : Set.copyOf(keywords);
        if (amountsRead == AmountsRead.BOOSTED && (subject.declaresTarget() || subject.isSet())) {
            throw new IllegalArgumentException("A boost reading the boosted permanent names one permanent without "
                    + "targeting it: " + subject);
        }
        if (amountsRead == AmountsRead.SOURCE && !subject.declaresTarget() && !isMassBoost(subject)
                && !(subject instanceof PermanentSubject.ThisWay)
                && !(subject instanceof PermanentSubject.Except)
                && !(subject instanceof PermanentSubject.CreatedThisResolution)
                && !(subject instanceof PermanentSubject.AttachedAndSharingCreatureType)) {
            throw new IllegalArgumentException("A boost reading its source boosts its target or what a choice named, "
                    + "left or created; another one-permanent subject is BoostEffect.of: " + subject);
        }
        if (!subject.declaresTarget() && (filter != null || targetsPermanent || targetGroup >= 0)) {
            throw new IllegalArgumentException("A target filter, kind or group needs a target subject: " + subject);
        }
        if (!keywords.isEmpty() && amountsRead != AmountsRead.BOOSTED) {
            throw new IllegalArgumentException("Only a boost of one named permanent grants keywords");
        }
    }

    /** "This creature gets +p/+t until end of turn." */
    public static BoostEffect self(int powerBoost, int toughnessBoost) {
        return self(new Fixed(powerBoost), new Fixed(toughnessBoost));
    }

    /** "This creature gets +p/+t" for {@code duration}. */
    public static BoostEffect self(int powerBoost, int toughnessBoost, Duration duration) {
        return self(new Fixed(powerBoost), new Fixed(toughnessBoost), duration);
    }

    /** "This creature gets +X/+Y until end of turn", or in the {@code STATIC} slot "this creature gets +1/+1 for each …". */
    public static BoostEffect self(DynamicAmount powerBoost, DynamicAmount toughnessBoost) {
        return self(powerBoost, toughnessBoost, Duration.UNTIL_END_OF_TURN);
    }

    public static BoostEffect self(DynamicAmount powerBoost, DynamicAmount toughnessBoost, Duration duration) {
        return new BoostEffect(powerBoost, toughnessBoost, Subjects.source(), AmountsRead.BOOSTED, duration, Set.of(),
                null, false, UNBOUND_TARGET_GROUP, null);
    }

    /** "Equipped creature gets +p/+t until end of turn", "it gets +p/+t until end of turn". */
    public static BoostEffect of(PermanentSubject subject, int powerBoost, int toughnessBoost) {
        return of(subject, new Fixed(powerBoost), new Fixed(toughnessBoost));
    }

    /** "It gets +p/+t and gains [keyword] until end of turn". */
    public static BoostEffect of(PermanentSubject subject, int powerBoost, int toughnessBoost, Keyword keyword) {
        return of(subject, new Fixed(powerBoost), new Fixed(toughnessBoost), Set.of(keyword));
    }

    /** "It gets +X/+Y until end of turn", the amounts read against that permanent. */
    public static BoostEffect of(PermanentSubject subject, DynamicAmount powerBoost, DynamicAmount toughnessBoost) {
        return of(subject, powerBoost, toughnessBoost, Set.of());
    }

    /** "It gets +X/+Y and gains [keywords] until end of turn". */
    public static BoostEffect of(PermanentSubject subject, DynamicAmount powerBoost, DynamicAmount toughnessBoost,
                                 Set<Keyword> keywords) {
        return of(subject, powerBoost, toughnessBoost, keywords, Duration.UNTIL_END_OF_TURN);
    }

    public static BoostEffect of(PermanentSubject subject, DynamicAmount powerBoost, DynamicAmount toughnessBoost,
                                 Set<Keyword> keywords, Duration duration) {
        return new BoostEffect(powerBoost, toughnessBoost, subject, AmountsRead.BOOSTED, duration, keywords,
                null, false, UNBOUND_TARGET_GROUP, null);
    }

    /** "Target creature gets +p/+t until end of turn." */
    public static BoostEffect target(int powerBoost, int toughnessBoost) {
        return target(new Fixed(powerBoost), new Fixed(toughnessBoost));
    }

    /** "Target creature an opponent controls gets -2/-0 until end of turn": the filter narrows the target. */
    public static BoostEffect target(int powerBoost, int toughnessBoost, PermanentPredicate filter) {
        return target(new Fixed(powerBoost), new Fixed(toughnessBoost), filter);
    }

    /** "Target creature gets -2/-0 until your next turn." */
    public static BoostEffect target(int powerBoost, int toughnessBoost, Duration duration) {
        return target(new Fixed(powerBoost), new Fixed(toughnessBoost), null, duration);
    }

    public static BoostEffect target(DynamicAmount powerBoost, DynamicAmount toughnessBoost) {
        return target(powerBoost, toughnessBoost, (PermanentPredicate) null);
    }

    public static BoostEffect target(DynamicAmount powerBoost, DynamicAmount toughnessBoost,
                                     PermanentPredicate filter) {
        return target(powerBoost, toughnessBoost, filter, Duration.UNTIL_END_OF_TURN);
    }

    public static BoostEffect target(DynamicAmount powerBoost, DynamicAmount toughnessBoost,
                                     PermanentPredicate filter, Duration duration) {
        return new BoostEffect(powerBoost, toughnessBoost, Subjects.target(), AmountsRead.SOURCE, duration, Set.of(),
                filter, false, UNBOUND_TARGET_GROUP, null);
    }

    /** An activated ability's positional target group gets +p/+t until end of turn. */
    public static BoostEffect targetInGroup(int powerBoost, int toughnessBoost, int targetGroup) {
        return targetInGroup(new Fixed(powerBoost), new Fixed(toughnessBoost), targetGroup);
    }

    public static BoostEffect targetInGroup(DynamicAmount powerBoost, DynamicAmount toughnessBoost, int targetGroup) {
        return new BoostEffect(powerBoost, toughnessBoost, Subjects.target(), AmountsRead.SOURCE,
                Duration.UNTIL_END_OF_TURN, Set.of(), null, false, targetGroup, null);
    }

    /** A target pump whose X is evaluated while the spell is cast and read through {@link XValue}. */
    public static BoostEffect withCastTimeXValue(DynamicAmount castTimeXValue, DynamicAmount powerBoost,
                                                 DynamicAmount toughnessBoost) {
        return new BoostEffect(powerBoost, toughnessBoost, Subjects.target(), AmountsRead.SOURCE,
                Duration.UNTIL_END_OF_TURN, Set.of(), null, false, UNBOUND_TARGET_GROUP, castTimeXValue);
    }

    /** "Target creature or Vehicle gets +p/+t until end of turn": a permanent target, the filter naming the kinds. */
    public static BoostEffect targetPermanent(int powerBoost, int toughnessBoost, PermanentPredicate filter) {
        return new BoostEffect(new Fixed(powerBoost), new Fixed(toughnessBoost), Subjects.target(), AmountsRead.SOURCE,
                Duration.UNTIL_END_OF_TURN, Set.of(), filter, true, UNBOUND_TARGET_GROUP, null);
    }

    /** Each creature {@code subject} names gets +p/+t until end of turn, the set found once (CR 611.2c). */
    public static BoostEffect each(PermanentSubject subject, int powerBoost, int toughnessBoost) {
        return each(subject, new Fixed(powerBoost), new Fixed(toughnessBoost));
    }

    /** "Each of them gets +X/+Y until end of turn": X and Y found once, from the source (CR 608.2h). */
    public static BoostEffect each(PermanentSubject subject, DynamicAmount powerBoost, DynamicAmount toughnessBoost) {
        return new BoostEffect(powerBoost, toughnessBoost, subject, AmountsRead.SOURCE, Duration.UNTIL_END_OF_TURN,
                Set.of(), null, false, UNBOUND_TARGET_GROUP, null);
    }

    /** "Creatures you control get +p/+t until end of turn." */
    public static BoostEffect creaturesYouControl(int powerBoost, int toughnessBoost) {
        return creaturesYouControl(new Fixed(powerBoost), new Fixed(toughnessBoost));
    }

    /** "[Filter] creatures you control get +p/+t until end of turn" ("other creatures you control", "Goblins you control"). */
    public static BoostEffect creaturesYouControl(int powerBoost, int toughnessBoost, PermanentPredicate filter) {
        return creaturesYouControl(new Fixed(powerBoost), new Fixed(toughnessBoost), filter);
    }

    /** "Creatures you control get +p/+t until your next turn." */
    public static BoostEffect creaturesYouControl(int powerBoost, int toughnessBoost, Duration duration) {
        return set(Subjects.creaturesYouControl(), new Fixed(powerBoost), new Fixed(toughnessBoost), duration);
    }

    /** "Creatures you control get +X/+Y until end of turn", X and Y found once, from the source (CR 608.2h). */
    public static BoostEffect creaturesYouControl(DynamicAmount powerBoost, DynamicAmount toughnessBoost) {
        return creaturesYouControl(powerBoost, toughnessBoost, null);
    }

    public static BoostEffect creaturesYouControl(DynamicAmount powerBoost, DynamicAmount toughnessBoost,
                                                  PermanentPredicate filter) {
        return set(Subjects.narrowed(Subjects.creaturesYouControl(), filter), powerBoost, toughnessBoost,
                Duration.UNTIL_END_OF_TURN);
    }

    /** "All creatures get +p/+t until end of turn." */
    public static BoostEffect allCreatures(int powerBoost, int toughnessBoost) {
        return allCreatures(new Fixed(powerBoost), new Fixed(toughnessBoost));
    }

    /** "[Filter] creatures get +p/+t until end of turn" ("attacking creatures", "creatures your opponents control"). */
    public static BoostEffect allCreatures(int powerBoost, int toughnessBoost, PermanentPredicate filter) {
        return allCreatures(new Fixed(powerBoost), new Fixed(toughnessBoost), filter);
    }

    /** "[Filter] creatures get +p/+t" for {@code duration} ("creatures your opponents control get -3/-0 until your next turn"). */
    public static BoostEffect allCreatures(int powerBoost, int toughnessBoost, PermanentPredicate filter,
                                           Duration duration) {
        return set(Subjects.narrowed(Subjects.allCreatures(), filter), new Fixed(powerBoost), new Fixed(toughnessBoost),
                duration);
    }

    /** "All creatures get +X/+Y until end of turn", X and Y found once, from the source (CR 608.2h). */
    public static BoostEffect allCreatures(DynamicAmount powerBoost, DynamicAmount toughnessBoost) {
        return allCreatures(powerBoost, toughnessBoost, null);
    }

    public static BoostEffect allCreatures(DynamicAmount powerBoost, DynamicAmount toughnessBoost,
                                           PermanentPredicate filter) {
        return set(Subjects.narrowed(Subjects.allCreatures(), filter), powerBoost, toughnessBoost,
                Duration.UNTIL_END_OF_TURN);
    }

    /** "Creatures target player controls get +p/+t until end of turn": declares the player target. */
    public static BoostEffect creaturesTargetPlayerControls(int powerBoost, int toughnessBoost) {
        return creaturesTargetPlayerControls(new Fixed(powerBoost), new Fixed(toughnessBoost), null);
    }

    /** "[Filter] creatures target player controls get +X/+Y until end of turn" ("each other creature that player controls"). */
    public static BoostEffect creaturesTargetPlayerControls(DynamicAmount powerBoost, DynamicAmount toughnessBoost,
                                                            PermanentPredicate filter) {
        return set(Subjects.narrowed(Subjects.creaturesTargetPlayerControls(), filter), powerBoost, toughnessBoost,
                Duration.UNTIL_END_OF_TURN);
    }

    private static BoostEffect set(PermanentSubject set, DynamicAmount powerBoost, DynamicAmount toughnessBoost,
                                   Duration duration) {
        return new BoostEffect(powerBoost, toughnessBoost, set, AmountsRead.SOURCE, duration, Set.of(), null, false,
                UNBOUND_TARGET_GROUP, null);
    }

    /**
     * Whether this boost gives one bonus to the creatures a set names ("creatures you control get", "all creatures
     * get", "those creatures get" of the attackers), found once as it resolves (CR 611.2c), and logs one summary line.
     */
    public boolean isMassBoost() {
        return isMassBoost(subject);
    }

    private static boolean isMassBoost(PermanentSubject subject) {
        return subject instanceof PermanentSubject.ControlledBy || subject instanceof PermanentSubject.TriggeringAttackers;
    }

    @Override
    public DynamicAmount castTimeXValue() {
        return castTimeXValue;
    }

    /**
     * The self-targeting marker for the source (and the source-or-host) form; a target subject declares a creature
     * target, or a permanent target for "target creature or Vehicle", harmful when the boost lowers power or toughness
     * (a negative fixed amount, or "-1/-1 for each ...") and benign otherwise; any other subject declares nothing, save
     * the player target of "creatures target player controls".
     */
    @Override
    public TargetSpec targetSpec() {
        if (subject instanceof PermanentSubject.Source || subject instanceof PermanentSubject.AttachedOrSource) {
            return new TargetSpec(null, false, null, true, 1);
        }
        if (subject.declaresPlayerTarget()) {
            return TargetSpec.benign(TargetPredicates.player());
        }
        if (!subject.declaresTarget()) {
            return TargetSpec.NONE;
        }
        TargetPredicate declared = targetsPermanent ? TargetPredicates.permanent() : TargetPredicates.creature();
        return lowers(powerBoost) || lowers(toughnessBoost) ? TargetSpec.harmful(declared, filter)
                : TargetSpec.benign(declared, filter);
    }

    /** A negative fixed amount, or a count scaled by a negative factor. */
    private static boolean lowers(DynamicAmount amount) {
        return amount instanceof Fixed fixed && fixed.value() < 0
                || amount instanceof Scaled scaled && scaled.factor() < 0;
    }

    /**
     * An Aura's or Equipment's ability boosting its host captures that host as it is activated, before a sacrifice or
     * return cost severs the attachment ("Sacrifice this Aura: Enchanted creature gets +3/+3", Briar Shield).
     */
    @Override
    public boolean resolvesAgainstAttachedPermanent() {
        return subject instanceof PermanentSubject.Attached || subject instanceof PermanentSubject.AttachedOrSource
                || subject instanceof PermanentSubject.AttachedAndSharingCreatureType;
    }

    @Override
    public boolean hasAbilityResolutionCondition() {
        return powerBoost.requiresAbilityResolutionCount() || toughnessBoost.requiresAbilityResolutionCount();
    }
}
