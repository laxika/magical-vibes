package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/**
 * Cost for removing a player-chosen number of counters (the activation's X) from matching permanents the player
 * controls.
 *
 * @param counterType        the kind of counter removed, or {@link CounterType#ANY}
 * @param permanentPredicate which permanents may pay, or {@code null} for any
 * @param allowsZero         whether X may be zero: "Remove X counters" (Moxite Refinery) may remove none, "one or more"
 *                           (Jetfire) needs one
 * @param fromOnePermanent   whether all X counters come from one permanent ("from an artifact or creature you
 *                           control", Moxite Refinery) rather than from among several ("from among artifacts you
 *                           control", Jetfire)
 */
public record RemoveOneOrMoreCountersFromControlledPermanentsCost(CounterType counterType,
                                                                  PermanentPredicate permanentPredicate,
                                                                  boolean allowsZero,
                                                                  boolean fromOnePermanent)
        implements CostEffect {

    /** "Remove one or more counters from among [permanents] you control". */
    public RemoveOneOrMoreCountersFromControlledPermanentsCost(CounterType counterType,
                                                               PermanentPredicate permanentPredicate) {
        this(counterType, permanentPredicate, false, false);
    }

    /** "Remove X counters from a [permanent] you control": X may be zero, all from the one chosen permanent. */
    public static RemoveOneOrMoreCountersFromControlledPermanentsCost xFromOnePermanent(
            CounterType counterType, PermanentPredicate permanentPredicate) {
        return new RemoveOneOrMoreCountersFromControlledPermanentsCost(counterType, permanentPredicate, true, true);
    }
}
