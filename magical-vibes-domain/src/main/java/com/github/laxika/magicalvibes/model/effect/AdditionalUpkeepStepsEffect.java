package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.amount.DynamicAmount;

import java.util.Objects;

/** Queues additional upkeep steps after the current combat phase. */
public record AdditionalUpkeepStepsEffect(DynamicAmount count) implements CardEffect {

    public AdditionalUpkeepStepsEffect {
        Objects.requireNonNull(count, "count");
    }
}
