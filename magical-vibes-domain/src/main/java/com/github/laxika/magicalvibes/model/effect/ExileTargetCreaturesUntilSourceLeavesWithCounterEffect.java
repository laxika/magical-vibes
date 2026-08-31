package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CounterType;

/**
 * Exiles any number of target creatures until the source leaves the battlefield, then creates a
 * delayed trigger for each returned card that enters under the captured controller's control.
 */
public record ExileTargetCreaturesUntilSourceLeavesWithCounterEffect(
        CounterType counterType, int counterAmount) implements CardEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.harmful(TargetPredicates.creature());
    }
}
