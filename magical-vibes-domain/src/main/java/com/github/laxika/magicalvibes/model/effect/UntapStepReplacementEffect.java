package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/** Replacement behavior for a permanent's normal untap-step untap. */
public interface UntapStepReplacementEffect extends CardEffect {

    CounterType counterType();

    /** The number of counters to put when this replacement does not remove counters. */
    default int replacementCount() {
        return 0;
    }

    /** Optional filter for the permanent whose untap is being replaced. */
    default PermanentPredicate filter() {
        return null;
    }

    /** Whether the replacement applies only to permanents controlled by this source's controller. */
    default boolean sourceControllerOnly() {
        return false;
    }

    /** Existing counter-removal replacements remove counters; other implementations put them. */
    default boolean removesCounters() {
        return true;
    }
}
