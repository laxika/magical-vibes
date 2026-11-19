package com.github.laxika.magicalvibes.model.effect;

/**
 * Schedules the permanents {@code subject} names for destruction at end of combat: at resolution a delayed "at end of
 * combat" ability is registered for each of them, which destroys it as a destruction, so regeneration and
 * indestructible apply. The subject is
 * <ul>
 *   <li>{@link Subjects#target()} (the default): a "destroy it at end of combat" rider on a spell or activated ability
 *       that also does something to that same target (Goblin Sappers' "Target creature you control can't be blocked
 *       this turn. Destroy it at end of combat."). Unlike {@link DestroyCombatOpponentAtEndOfCombatEffect} this is a
 *       real target chosen on activation;</li>
 *   <li>{@link Subjects#triggering()} ({@link #ofTriggeringPermanent()}): "whenever this creature deals combat damage
 *       to a creature, destroy that creature at end of combat" (the basilisks, Sosuke, Son of Seshiro);</li>
 *   <li>{@link Subjects#source()}: "when this creature blocks, destroy it at end of combat" (Cinder Wall, Arrogant
 *       Bloodlord). Unlike {@link SacrificeAtEndOfCombatEffect} this is a destruction.</li>
 * </ul>
 * Only the target subject targets.
 *
 * @param cannotBeRegenerated whether the scheduled destruction ignores regeneration shields
 * @param subject             which permanents are scheduled
 */
public record DestroyTargetPermanentAtEndOfCombatEffect(boolean cannotBeRegenerated, PermanentSubject subject)
        implements CardEffect, RemovalEffect {

    public DestroyTargetPermanentAtEndOfCombatEffect {
        if (subject == null) {
            subject = Subjects.target();
        }
    }

    public DestroyTargetPermanentAtEndOfCombatEffect() {
        this(false);
    }

    public DestroyTargetPermanentAtEndOfCombatEffect(boolean cannotBeRegenerated) {
        this(cannotBeRegenerated, Subjects.target());
    }

    /** Schedules what {@code subject} names; regeneration still applies. */
    public DestroyTargetPermanentAtEndOfCombatEffect(PermanentSubject subject) {
        this(false, subject);
    }

    /** "Destroy that creature at end of combat": the stack entry's triggering permanent. */
    public static DestroyTargetPermanentAtEndOfCombatEffect ofTriggeringPermanent() {
        return new DestroyTargetPermanentAtEndOfCombatEffect(Subjects.triggering());
    }

    /**
     * Removal of another permanent (the target, or the creature a basilisk dealt damage to); the source's own delayed
     * destruction ("destroy this creature at end of combat") is not removal.
     */
    @Override
    public RemovalKind removalKind() {
        return subject instanceof PermanentSubject.Source ? null : RemovalKind.DESTROY;
    }

    @Override
    public TargetSpec targetSpec() {
        if (!subject.declaresTarget()) {
            return TargetSpec.NONE;
        }
        return TargetSpec.harmful(TargetPredicates.creature());
    }
}
