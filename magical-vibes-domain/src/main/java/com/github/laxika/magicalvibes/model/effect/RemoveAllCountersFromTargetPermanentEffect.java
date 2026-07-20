package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CounterType;

/**
 * "Remove all [counterType] counters from target permanent." Removes every counter of exactly the
 * given type from the targeted permanent; no-op when it carries none. Target category is
 * {@link TargetCategory#CREATURE} (benign — -1/-1 counters live on creatures), the card/ability's
 * explicit target filter narrows further ("target creature you control" on Hapatra's Mark).
 */
public record RemoveAllCountersFromTargetPermanentEffect(CounterType counterType) implements CardEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetCategory.CREATURE);
    }
}
