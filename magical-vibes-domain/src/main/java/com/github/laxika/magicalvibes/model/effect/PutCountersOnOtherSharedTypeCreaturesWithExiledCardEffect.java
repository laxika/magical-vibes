package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CounterType;

/**
 * "When you do, put a +1/+1 counter on each creature you control other than this creature that shares a creature
 * type with the exiled card": puts counters on each creature the controller controls other than the source that
 * shares a creature type with the card the resolution's most recent EXILE step exiled, which the reflexive ability
 * carries with its step results (Resplendent Marshal).
 */
public record PutCountersOnOtherSharedTypeCreaturesWithExiledCardEffect(
        CounterType counterType,
        int counterCount
) implements CardEffect {
}
