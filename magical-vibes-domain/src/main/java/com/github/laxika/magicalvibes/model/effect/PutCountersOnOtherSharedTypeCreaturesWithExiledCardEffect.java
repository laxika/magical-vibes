package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CounterType;

/**
 * Reflexive follow-up of {@link ExileOwnCardFromGraveyardThenPutCountersOnSharedTypeCreaturesEffect}:
 * puts counters on each creature the controller controls other than the source that shares a creature
 * type with the card exiled by the first part, which the queued reflexive ability carries as its
 * exiled-card snapshot.
 */
public record PutCountersOnOtherSharedTypeCreaturesWithExiledCardEffect(
        CounterType counterType,
        int counterCount
) implements CardEffect {
}
