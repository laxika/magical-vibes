package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

import java.util.UUID;

/** Chooses one eligible land at resolution and removes all of its mire counters. */
public record RemoveAllMireCountersFromChosenLandEffect(
        UUID delayedActionId,
        PermanentPredicate permanentFilter
) implements CardEffect {
}
