package com.github.laxika.magicalvibes.model.effect;

/**
 * "The next time target creature adapts this turn, it adapts as though it had no +1/+1 counters on it" (Biomancer's
 * Familiar): a floating effect on the target until end of turn, used up by the next Adapt
 * ({@code GameData.useAdaptOverride}).
 */
public record AllowTargetCreatureToAdaptEffect() implements CardEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetPredicates.creature());
    }
}
