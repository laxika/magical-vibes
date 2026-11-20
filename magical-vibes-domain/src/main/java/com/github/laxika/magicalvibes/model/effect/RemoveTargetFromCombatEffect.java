package com.github.laxika.magicalvibes.model.effect;

/**
 * Removes the permanents {@code subject} names from combat (CR 506.4): an attacking creature stops attacking, a
 * blocking creature stops blocking, and an attacker it blocked stays blocked (CR 509.1h).
 *
 * <p>Subjects: {@code target()} ("remove target creature from combat", the only one that declares a target, a benign
 * creature), {@code source()} ("remove it from combat" in this creature's own ability; Gustcloak Runner, Mijae
 * Djinn), {@code triggering()} ("untap that creature and remove it from combat"; Gustcloak Savior, Melee's delayed
 * ability), {@code attached()} ("remove enchanted creature from combat"; Observed Stasis). A {@code null} subject is
 * {@code target()}.
 *
 * @param unblockSoleBlockedAttackers when true, attackers the removed creature was blocking that had become blocked by
 *                                    only that creature this combat become unblocked (Imprison)
 */
public record RemoveTargetFromCombatEffect(PermanentSubject subject, boolean unblockSoleBlockedAttackers)
        implements CardEffect {

    public RemoveTargetFromCombatEffect {
        subject = subject == null ? Subjects.target() : subject;
    }

    public RemoveTargetFromCombatEffect() {
        this(Subjects.target(), false);
    }

    /**
     * @param triggeringPermanent whether it acts on the stack entry's triggering permanent without targeting it
     *                            ("untap it and remove it from combat", Melee)
     */
    public RemoveTargetFromCombatEffect(boolean triggeringPermanent) {
        this(triggeringPermanent ? Subjects.triggering() : Subjects.target(), false);
    }

    public RemoveTargetFromCombatEffect(PermanentSubject subject) {
        this(subject, false);
    }

    /** The permanent the trigger event was about is removed from combat. */
    public static RemoveTargetFromCombatEffect ofTriggeringPermanent() {
        return new RemoveTargetFromCombatEffect(Subjects.triggering());
    }

    @Override
    public TargetSpec targetSpec() {
        return subject.declaresTarget() ? TargetSpec.benign(TargetPredicates.creature()) : TargetSpec.NONE;
    }
}
