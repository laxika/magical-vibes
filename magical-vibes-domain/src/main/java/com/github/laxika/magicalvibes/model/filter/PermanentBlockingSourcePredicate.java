package com.github.laxika.magicalvibes.model.filter;

import com.github.laxika.magicalvibes.model.effect.PermanentSubject;
import com.github.laxika.magicalvibes.model.effect.Subjects;

/**
 * Creatures blocking the source permanent — "target creature blocking this creature"
 * (Barbed-Back Wurm). Mirror image of {@link PermanentBlockedBySourcePredicate} and narrower
 * than {@link PermanentInCombatWithSourcePredicate}, which matches both directions.
 *
 * <p>With {@link Subjects#triggering()} it is the creatures blocking the permanent the trigger event was
 * about ("a creature … that's blocking it" of "whenever a creature you control becomes blocked", Labyrinth Raptor):
 * the filter context's triggering permanent, or its last-known information once it has left.
 *
 * @param blocked {@link Subjects#source()} or {@link Subjects#triggering()}
 */
public record PermanentBlockingSourcePredicate(PermanentSubject blocked) implements PermanentPredicate {

    public PermanentBlockingSourcePredicate {
        if (!(blocked instanceof PermanentSubject.Source) && !(blocked instanceof PermanentSubject.Triggering)) {
            throw new IllegalArgumentException("Only the source or the triggering permanent: " + blocked);
        }
    }

    public PermanentBlockingSourcePredicate() {
        this(Subjects.source());
    }

    /** Creatures blocking the permanent the trigger event was about. */
    public static PermanentBlockingSourcePredicate ofTriggeringPermanent() {
        return new PermanentBlockingSourcePredicate(Subjects.triggering());
    }
}
