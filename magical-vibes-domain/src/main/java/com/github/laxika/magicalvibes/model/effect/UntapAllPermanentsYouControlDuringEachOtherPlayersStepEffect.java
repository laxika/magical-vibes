package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/**
 * "Untap [these permanents] during each other player's untap step", a static ability: during the untap step of each
 * player other than the controller of a permanent the subject names, that permanent untaps.
 *
 * @param step    the step during which this applies (always UNTAP)
 * @param subject {@code controlledBy(you(), filter)} for every matching permanent the source's controller controls
 *                (Seedborn Muse, Unwinding Clock; an emblem's too), {@code attached()} for the permanent this Aura
 *                is attached to, whoever controls the Aura (Urban Burgeoning), or {@code source()} for the source
 *                permanent itself (Bender's Waterskin)
 */
public record UntapAllPermanentsYouControlDuringEachOtherPlayersStepEffect(TurnStep step, PermanentSubject subject)
        implements CardEffect {

    public UntapAllPermanentsYouControlDuringEachOtherPlayersStepEffect {
        boolean ownSet = subject instanceof PermanentSubject.ControlledBy set
                && set.players() instanceof PlayerSubject.You;
        if (!ownSet && !(subject instanceof PermanentSubject.Attached)
                && !(subject instanceof PermanentSubject.Source)) {
            throw new IllegalArgumentException("Unsupported subject for cross-player untap: " + subject);
        }
    }

    /** Every permanent the controller controls that matches {@code filter} ({@code null}: every permanent). */
    public UntapAllPermanentsYouControlDuringEachOtherPlayersStepEffect(TurnStep step, PermanentPredicate filter) {
        this(step, filter == null ? Subjects.controlledBy(Subjects.you())
                : Subjects.controlledBy(Subjects.you(), filter));
    }

    /** Every permanent the controller controls. */
    public UntapAllPermanentsYouControlDuringEachOtherPlayersStepEffect(TurnStep step) {
        this(step, (PermanentPredicate) null);
    }
}
