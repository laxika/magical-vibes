package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CounterType;

/**
 * Put one or more counters of the specified type on the creature enchanted by the source aura.
 */
public record PutCounterOnEnchantedCreatureEffect(CounterType counterType, int count) implements CardEffect {

    public PutCounterOnEnchantedCreatureEffect(CounterType counterType) {
        this(counterType, 1);
    }
}
