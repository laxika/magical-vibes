package com.github.laxika.magicalvibes.model.effect;

/**
 * Turns the source permanent face up without paying a turn-face-up cost, or, for
 * {@link #ofTriggeringPermanent()}, the permanent the trigger event was about ("turn that creature
 * face up", read from the stack entry's triggering permanent as the ability resolves).
 *
 * @param reference {@link PermanentReference#SOURCE} or {@link PermanentReference#TRIGGERING}
 */
public record TurnSourceFaceUpEffect(PermanentReference reference) implements CardEffect {

    public TurnSourceFaceUpEffect {
        if (reference != PermanentReference.SOURCE && reference != PermanentReference.TRIGGERING) {
            throw new IllegalArgumentException("Turns the source or the triggering permanent face up: " + reference);
        }
    }

    public TurnSourceFaceUpEffect() {
        this(PermanentReference.SOURCE);
    }

    /** "Turn that creature face up": the stack entry's triggering permanent. */
    public static TurnSourceFaceUpEffect ofTriggeringPermanent() {
        return new TurnSourceFaceUpEffect(PermanentReference.TRIGGERING);
    }
}
