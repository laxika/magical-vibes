package com.github.laxika.magicalvibes.model.effect;

/**
 * Ally-creature-death trigger marker for "exile that many cards ... until your next end step",
 * where "that many" is the total number of counters on the dying creature.
 */
public record ExileTopCardsForEachDyingCreatureCounterMayPlayUntilNextEndStepEffect()
        implements CardEffect, DyingCreatureCounterAwareEffect {

    @Override
    public CardEffect boundToDyingCreatureCounterCount(int counterCount) {
        return new ExileTopCardsMayPlayUntilNextEndStepEffect(counterCount);
    }
}
