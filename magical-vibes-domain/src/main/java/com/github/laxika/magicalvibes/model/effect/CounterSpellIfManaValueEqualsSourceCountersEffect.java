package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CounterType;

/**
 * Trigger descriptor for countering the triggering spell when its mana value equals the number
 * of the specified counters on the source permanent.
 *
 * <p>The equality is checked as the spell is cast. The trigger collector snapshots the spell's
 * mana value and targets that spell, so changing the source's counters before the trigger resolves
 * does not undo an already-triggered ability.</p>
 */
public record CounterSpellIfManaValueEqualsSourceCountersEffect(
        CounterType counterType,
        int manaValueAtTrigger
) implements CounterSpellingEffect {

    public CounterSpellIfManaValueEqualsSourceCountersEffect(CounterType counterType) {
        this(counterType, -1);
    }

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetPredicates.spellOnStack());
    }
}
