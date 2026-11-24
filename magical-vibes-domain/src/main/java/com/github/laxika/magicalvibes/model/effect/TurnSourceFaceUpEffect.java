package com.github.laxika.magicalvibes.model.effect;

import java.util.Objects;

/**
 * Turns a face-down permanent face up without paying a turn-face-up cost: the source, the permanent the trigger event
 * was about ({@link #ofTriggeringPermanent()}: "turn that creature face up", read as the ability resolves), or the
 * target ({@link #ofTarget()}: "turn target face-down creature face up", "turn it face up if it's face down"). One
 * that isn't face down is left alone; one that can't be turned face up stays face down (CR 101.2); a face-down
 * instant or sorcery card is revealed and stays face down (CR 701.40g). Its "enters" abilities don't trigger
 * (CR 708.8).
 *
 * @param subject {@link Subjects#source()}, {@link Subjects#triggering()} or {@link Subjects#target()} (a benign
 *                creature target; the card's target filter says whether it must be face down)
 */
public record TurnSourceFaceUpEffect(PermanentSubject subject) implements CardEffect {

    public TurnSourceFaceUpEffect {
        Objects.requireNonNull(subject, "subject");
        if (!(subject instanceof PermanentSubject.Source) && !(subject instanceof PermanentSubject.Triggering)
                && !(subject instanceof PermanentSubject.Target)) {
            throw new IllegalArgumentException("Turns the source, the triggering permanent or the target face up: "
                    + subject);
        }
    }

    public TurnSourceFaceUpEffect() {
        this(Subjects.source());
    }

    /** "Turn that creature face up": the stack entry's triggering permanent. */
    public static TurnSourceFaceUpEffect ofTriggeringPermanent() {
        return new TurnSourceFaceUpEffect(Subjects.triggering());
    }

    /** "Turn target face-down creature face up." */
    public static TurnSourceFaceUpEffect ofTarget() {
        return new TurnSourceFaceUpEffect(Subjects.target());
    }

    @Override
    public TargetSpec targetSpec() {
        return subject.declaresTarget() ? TargetSpec.benign(TargetPredicates.creature()) : TargetSpec.NONE;
    }
}
