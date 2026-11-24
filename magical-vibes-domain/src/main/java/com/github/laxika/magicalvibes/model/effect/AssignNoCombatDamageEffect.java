package com.github.laxika.magicalvibes.model.effect;

import java.util.Objects;

/**
 * "[Creature] assigns no combat damage this turn": a floating effect that prevents its combat damage until end of turn,
 * and another that makes it assign none (this record is its payload, {@code GameData.assignsNoCombatDamage}), both
 * ending at cleanup. Pair with damage (or similar) inside a {@link MayEffect}/{@link SequenceEffect} for "you may
 * [effect]. If you do, it assigns no combat damage this turn" (Gaze of Pain). It applies to the creature as it is named
 * even if it has left the battlefield.
 *
 * @param subject        the creature: {@code source()} (this creature), {@code triggering()} ("the attacking
 *                       creature", the enchanted attacker of an Aura's trigger), {@code attachedAtTrigger()} ("the
 *                       first creature" of an Aura's trigger: the host as the ability triggered, Laccolith Rig)
 * @param thisCombatOnly the restriction ends with the current combat instead of lasting the whole turn ("assigns no
 *                       combat damage this combat")
 */
public record AssignNoCombatDamageEffect(PermanentSubject subject, boolean thisCombatOnly) implements CardEffect {

    public AssignNoCombatDamageEffect {
        Objects.requireNonNull(subject, "subject");
        if (subject.declaresTarget()) {
            throw new IllegalArgumentException("Assigning no combat damage targets nothing: " + subject);
        }
    }

    /** "This creature assigns no combat damage this turn." */
    public AssignNoCombatDamageEffect() {
        this(Subjects.source(), false);
    }

    /** "[Creature] assigns no combat damage this turn." */
    public static AssignNoCombatDamageEffect of(PermanentSubject subject) {
        return new AssignNoCombatDamageEffect(subject, false);
    }

    /**
     * The permanent the trigger event was about assigns no combat damage this turn ("the attacking creature assigns no
     * combat damage", Farrel's Mantle's enchanted creature).
     */
    public static AssignNoCombatDamageEffect ofTriggeringPermanent() {
        return of(Subjects.triggering());
    }

    /** The source permanent assigns no combat damage this combat only (Master of Cruelties). */
    public static AssignNoCombatDamageEffect thisCombat() {
        return new AssignNoCombatDamageEffect(Subjects.source(), true);
    }
}
