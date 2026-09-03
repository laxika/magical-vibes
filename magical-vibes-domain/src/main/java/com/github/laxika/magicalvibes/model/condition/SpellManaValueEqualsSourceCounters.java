package com.github.laxika.magicalvibes.model.condition;

import com.github.laxika.magicalvibes.model.CounterType;

/** The triggering spell's mana value equals the source's counter count plus {@code offset}. */
public record SpellManaValueEqualsSourceCounters(CounterType counterType, int offset)
        implements Condition {

    public SpellManaValueEqualsSourceCounters(CounterType counterType) {
        this(counterType, 0);
    }

    @Override
    public String conditionName() {
        return "spell mana value equals " + counterType.name().toLowerCase()
                + " counters on source plus " + offset;
    }

    @Override
    public String conditionNotMetReason() {
        return "spell mana value does not equal the source's " + counterType.name().toLowerCase()
                + " counters plus " + offset;
    }
}
