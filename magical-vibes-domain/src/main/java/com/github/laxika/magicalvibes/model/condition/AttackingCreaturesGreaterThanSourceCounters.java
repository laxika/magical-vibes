package com.github.laxika.magicalvibes.model.condition;

import com.github.laxika.magicalvibes.model.CounterType;

/** True when the trigger-time attacker count exceeds the source's counter count. */
public record AttackingCreaturesGreaterThanSourceCounters(CounterType counterType) implements Condition {

    @Override
    public String conditionName() {
        return "more attacking creatures than " + counterType.name().toLowerCase() + " counters on this permanent";
    }

    @Override
    public String conditionNotMetReason() {
        return "the number of attacking creatures was not greater than the source's counters";
    }
}
