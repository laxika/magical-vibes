package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CounterType;

/**
 * Cost that removes {@code count} counters of {@code counterType} from the source permanent
 * ("Remove three charge counters from this artifact: …", "Remove a -1/-1 counter from this
 * creature: …"). {@link CounterType#ANY} spends any counters on the permanent, prioritizing
 * -1/-1 and then +1/+1 counters before other types. Validated and paid in
 * {@code AbilityActivationService}.
 */
public record RemoveCounterFromSourceCost(int count, CounterType counterType) implements CostEffect {

    public RemoveCounterFromSourceCost() {
        this(1, CounterType.ANY);
    }

    public RemoveCounterFromSourceCost(int count) {
        this(count, CounterType.ANY);
    }

    /**
     * Every counter this cost spends is a use the source permanent no longer has, so the whole
     * {@code count} is reported. The AI's {@code SpellEvaluator} weighs each removed counter as one
     * point of cost regardless of type; that is right for the limited-resource counters this cost
     * mostly spends (charge, wish, study, divinity, …) and understates the value of the +1/+1 the
     * source gains back when the type is {@code MINUS_ONE_MINUS_ONE} — a shortcoming of the scoring
     * consumer, not of this fact.
     */
    @Override
    public int sourceCountersRemoved() {
        return count;
    }
}
