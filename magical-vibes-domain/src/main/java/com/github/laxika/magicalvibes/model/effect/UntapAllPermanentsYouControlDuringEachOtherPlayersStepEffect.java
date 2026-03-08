package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/**
 * Untap permanents you control during each other player's untap step.
 *
 * @param step   the step during which this fires (always UNTAP)
 * @param filter optional filter for which permanents to untap (null = all permanents)
 */
public record UntapAllPermanentsYouControlDuringEachOtherPlayersStepEffect(TurnStep step,
                                                                           PermanentPredicate filter) implements CardEffect {

    /**
     * Convenience constructor that untaps ALL permanents (no filter).
     */
    public UntapAllPermanentsYouControlDuringEachOtherPlayersStepEffect(TurnStep step) {
        this(step, null);
    }
}
