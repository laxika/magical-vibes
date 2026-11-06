package com.github.laxika.magicalvibes.model.effect;

/**
 * Secrets of Strixhaven "Prepared": the source permanent becomes prepared.
 * <p>
 * When this resolves, a copy of the source's prepare spell (its {@code backFaceCard}) is created in
 * exile with a non-expiring play permission for the source's controller, and the source permanent is
 * marked prepared. While prepared, the controller may cast that exiled copy; doing so unprepares the
 * permanent (handled at cast time, not resolution). A permanent that is already prepared can't become
 * prepared again, so this is a no-op in that case (CR-style: never more than one prepare copy at once).
 * <p>
 * This is normally a self-targeting effect so the trigger framework stamps the source permanent id
 * onto the resolving stack entry (see {@code SpellCastTriggerCollectorService}).
 *
 * @param asTriggeredAbility        "when this creature enters, it becomes prepared": a triggered ability that goes on
 *                                  the stack, not applied as the permanent enters
 * @param preparesGrantingPermanent the permanent that becomes prepared is the one that granted the triggered ability
 *                                  this effect is part of, the stack entry's granting permanent
 *                                  ({@code StackEntry.getGrantingPermanentId()}), not the ability's source: Stensian
 *                                  Sanguinist's "whenever that creature deals combat damage to a player this combat,
 *                                  this creature becomes prepared"
 */
public record BecomePreparedEffect(boolean asTriggeredAbility, boolean preparesGrantingPermanent)
        implements CardEffect {

    public BecomePreparedEffect() {
        this(false, false);
    }

    /**
     * "When this creature enters, it becomes prepared": a triggered ability that goes on the stack,
     * unlike "this creature enters prepared", which is applied as the permanent enters.
     */
    public static BecomePreparedEffect triggerOnly() {
        return new BecomePreparedEffect(true, false);
    }

    /** "This creature becomes prepared" in an ability this creature grants another permanent. */
    public static BecomePreparedEffect ofGrantingPermanent() {
        return new BecomePreparedEffect(false, true);
    }

    @Override
    public TargetSpec targetSpec() {
        return new TargetSpec(null, false, null, true, 1);
    }
}
