package com.github.laxika.magicalvibes.model.effect;

/**
 * "Put an Aura card from your hand onto the battlefield attached to" the permanent {@code subject} names: this creature
 * ({@link Subjects#source()}, the default, Academy Researchers) or the creature the trigger event was about
 * ({@link Subjects#triggering()}: "attached to it", Holy Avenger).
 *
 * @param subject what the Aura is attached to
 */
public record PutAuraFromHandOntoSelfEffect(PermanentSubject subject) implements CardEffect {

    public PutAuraFromHandOntoSelfEffect {
        if (!(subject instanceof PermanentSubject.Source || subject instanceof PermanentSubject.Triggering)) {
            throw new IllegalArgumentException("Attaches to the source or the triggering permanent: " + subject);
        }
    }

    public PutAuraFromHandOntoSelfEffect() {
        this(Subjects.source());
    }
}
