package com.github.laxika.magicalvibes.model.effect;

/**
 * Grants a targeting restriction until end of turn to the target permanent (Vines of Vastwood), or makes it a rule of
 * the set {@code subject} names ({@link Subjects#creaturesYouControl()}, Veilstone Amulet): that covers creatures
 * arriving later (CR 611.2c) and doesn't target.
 *
 * <p>This is separate from a keyword grant because some cards create a hexproof-like restriction
 * without granting the hexproof keyword or its special interactions.</p>
 */
public record GrantTargetingRestrictionToTargetUntilEndOfTurnEffect(
        TargetingRestrictionEffect restriction, PermanentSubject subject) implements CardEffect {

    public GrantTargetingRestrictionToTargetUntilEndOfTurnEffect {
        if (!(subject instanceof PermanentSubject.Target || subject instanceof PermanentSubject.ControlledBy)
                || subject.declaresPlayerTarget()) {
            throw new IllegalArgumentException("Restricts targeting of the target or of a set: " + subject);
        }
    }

    public GrantTargetingRestrictionToTargetUntilEndOfTurnEffect(TargetingRestrictionEffect restriction) {
        this(restriction, Subjects.target());
    }

    @Override
    public TargetSpec targetSpec() {
        return subject.declaresTarget() ? TargetSpec.benign(TargetPredicates.permanent()) : TargetSpec.NONE;
    }
}
