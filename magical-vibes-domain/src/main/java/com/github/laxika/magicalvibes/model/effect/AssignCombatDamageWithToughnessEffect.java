package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/**
 * Static effect: creatures assign combat damage equal to their toughness rather than their power.
 *
 * <p>The {@code subject} determines which creatures are affected:
 * <ul>
 *   <li>{@link Subjects#source()} — only the source creature; always uses toughness regardless of
 *       power.</li>
 *   <li>{@link Subjects#attachedCreature()} / {@link Subjects#attachedCreature()} —
 *       only the attached creature; also requires toughness &gt; power (equipment/aura pattern,
 *       e.g. Bark of Doran).</li>
 *   <li>{@link Subjects#otherCreaturesYouControl()} — all other creatures the controller controls;
 *       always uses toughness regardless of power (e.g. Belligerent Brontodon).</li>
 *   <li>{@link Subjects#creaturesYouControl()} — all creatures the controller controls
 *       including the source; always uses toughness regardless of power.</li>
 *   <li>{@link Subjects#otherCreatures()} — every creature on the battlefield regardless of
 *       controller; always uses toughness regardless of power (e.g. Doran, the Siege Tower).</li>
 * </ul>
 */
public record AssignCombatDamageWithToughnessEffect(
        PermanentSubject subject,
        PermanentPredicate affectedPredicate,
        boolean alwaysUseToughness
)
        implements CombatDamageAssignmentEffect {

    @Override
    public CombatDamageAssignmentMode assignmentMode() {
        return CombatDamageAssignmentMode.TOUGHNESS;
    }

    public AssignCombatDamageWithToughnessEffect(PermanentSubject subject) {
        this(subject, null, false);
    }

    public AssignCombatDamageWithToughnessEffect(PermanentSubject subject, boolean alwaysUseToughness) {
        this(subject, null, alwaysUseToughness);
    }

    public AssignCombatDamageWithToughnessEffect(PermanentSubject subject, PermanentPredicate affectedPredicate) {
        this(subject, affectedPredicate, false);
    }

    /**
     * Backwards-compatible no-arg constructor defaulting to {@link Subjects#attachedCreature()}.
     */
    public AssignCombatDamageWithToughnessEffect() {
        this(Subjects.attachedCreature(), null, false);
    }
}
