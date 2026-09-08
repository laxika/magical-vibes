package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CounterType;

import java.util.Arrays;
import java.util.List;

/**
 * As the source creature enters, its controller must exile exactly X creature cards from their
 * graveyard. If that is not possible, the creature is put into its owner's graveyard instead.
 * For each card exiled this way, the controller chooses one of the supplied counter types and the
 * creature gets one counter of that type.
 */
public record ExileXCreatureCardsFromGraveyardOnEnterWithCountersEffect(List<CounterType> counterTypes)
        implements ReplacementEffect {

    public ExileXCreatureCardsFromGraveyardOnEnterWithCountersEffect(CounterType... counterTypes) {
        this(Arrays.asList(counterTypes));
    }

    public ExileXCreatureCardsFromGraveyardOnEnterWithCountersEffect {
        counterTypes = List.copyOf(counterTypes);
        if (counterTypes.isEmpty()) {
            throw new IllegalArgumentException("At least one counter type is required");
        }
    }
}
