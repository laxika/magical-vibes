package com.github.laxika.magicalvibes.model.effect;

import java.util.Objects;

/**
 * Secrets of Strixhaven "Prepared": a permanent (the source, by default) becomes prepared.
 * <p>
 * When this resolves, a copy of the source's prepare spell (its {@code backFaceCard}) is created in
 * exile with a non-expiring play permission for the source's controller, and the source permanent is
 * marked prepared. While prepared, the controller may cast that exiled copy; doing so unprepares the
 * permanent (handled at cast time, not resolution). A permanent that is already prepared can't become
 * prepared again, so this is a no-op in that case (CR-style: never more than one prepare copy at once).
 * <p>
 * This is normally a self-targeting effect so the trigger framework stamps the source permanent id
 * onto the resolving stack entry (see {@code SpellTriggers}).
 *
 * @param asTriggeredAbility "when this creature enters, it becomes prepared": a triggered ability that goes on the
 *                           stack, not applied as the permanent enters
 * @param subject            the permanent that becomes prepared: {@code source()}; {@code granter()}, the permanent
 *                           that granted the triggered ability this effect is part of (Stensian Sanguinist's "whenever
 *                           that creature deals combat damage to a player this combat, this creature becomes
 *                           prepared"); {@code target()}, a benign creature target ("target creature becomes prepared",
 *                           Biblioplex Tomekeeper, Skycoach Waypoint). The prepare copy goes to that permanent's
 *                           controller.
 */
public record BecomePreparedEffect(boolean asTriggeredAbility, PermanentSubject subject) implements CardEffect {

    public BecomePreparedEffect {
        Objects.requireNonNull(subject, "subject");
        if (asTriggeredAbility && !(subject instanceof PermanentSubject.Source)) {
            throw new IllegalArgumentException("Only the source becomes prepared as it enters: " + subject);
        }
    }

    public BecomePreparedEffect() {
        this(false, Subjects.source());
    }

    /**
     * "When this creature enters, it becomes prepared": a triggered ability that goes on the
     * stack, unlike "this creature enters prepared", which is applied as the permanent enters.
     */
    public static BecomePreparedEffect triggerOnly() {
        return new BecomePreparedEffect(true, Subjects.source());
    }

    /** "This creature becomes prepared" in an ability this creature grants another permanent. */
    public static BecomePreparedEffect ofGrantingPermanent() {
        return new BecomePreparedEffect(false, Subjects.granter());
    }

    /** "Target creature becomes prepared." */
    public static BecomePreparedEffect ofTarget() {
        return new BecomePreparedEffect(false, Subjects.target());
    }

    @Override
    public TargetSpec targetSpec() {
        return subject.declaresTarget()
                ? TargetSpec.benign(TargetPredicates.creature())
                : new TargetSpec(null, false, null, true, 1);
    }
}
