package com.github.laxika.magicalvibes.model.effect;

/**
 * Turns the source permanent face up without paying a turn-face-up cost, or, for
 * {@link #ofTriggeringPermanent()}, the permanent the trigger event was about ("turn that creature
 * face up", read from the stack entry's triggering permanent as the ability resolves).
 *
 * @param subject {@link Subjects#source()} or {@link Subjects#triggering()}
 */
public record TurnSourceFaceUpEffect(PermanentSubject subject) implements CardEffect {

    public TurnSourceFaceUpEffect {
        if (!(subject instanceof PermanentSubject.Source) && !(subject instanceof PermanentSubject.Triggering)) {
            throw new IllegalArgumentException("Turns the source or the triggering permanent face up: " + subject);
        }
    }

    public TurnSourceFaceUpEffect() {
        this(Subjects.source());
    }

    /** "Turn that creature face up": the stack entry's triggering permanent. */
    public static TurnSourceFaceUpEffect ofTriggeringPermanent() {
        return new TurnSourceFaceUpEffect(Subjects.triggering());
    }
}
