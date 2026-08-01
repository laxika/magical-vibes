package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/**
 * Untap permanents during each other player's untap step.
 *
 * @param step   the step during which this fires (always UNTAP)
 * @param filter optional filter for which permanents to untap (null = all permanents); ignored for
 *               {@link TapUntapScope#ENCHANTED}
 * @param scope  {@link TapUntapScope#CONTROLLED} — every matching permanent the source's controller
 *               controls (Seedborn Muse, Unwinding Clock) — or {@link TapUntapScope#ENCHANTED} —
 *               only the permanent this aura is attached to (Urban Burgeoning)
 */
public record UntapAllPermanentsYouControlDuringEachOtherPlayersStepEffect(TurnStep step,
                                                                          PermanentPredicate filter,
                                                                          TapUntapScope scope) implements CardEffect {

    public UntapAllPermanentsYouControlDuringEachOtherPlayersStepEffect {
        if (scope != TapUntapScope.CONTROLLED && scope != TapUntapScope.ENCHANTED) {
            throw new IllegalArgumentException("Unsupported scope for cross-player untap: " + scope);
        }
    }

    /**
     * Convenience constructor for the controller-scoped variant with an optional filter.
     */
    public UntapAllPermanentsYouControlDuringEachOtherPlayersStepEffect(TurnStep step, PermanentPredicate filter) {
        this(step, filter, TapUntapScope.CONTROLLED);
    }

    /**
     * Convenience constructor that untaps ALL permanents the controller controls (no filter).
     */
    public UntapAllPermanentsYouControlDuringEachOtherPlayersStepEffect(TurnStep step) {
        this(step, null, TapUntapScope.CONTROLLED);
    }
}
