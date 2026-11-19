package com.github.laxika.magicalvibes.model.effect;

/**
 * "Target spell's controller exiles it with X delay counters on it." (Ertai's Meddling)
 *
 * <p>X comes from the stack entry's X value. The spell leaves the stack without being countered —
 * "can't be countered" does not stop this — and the card is exiled with X delay counters
 * ({@code GameData.exiledCardDelayCounters}). The resolution creates the delayed triggered ability "at the beginning
 * of each of that player's upkeeps, if that card is exiled, ..." ({@link RemoveDelayCounterFromExiledSpellEffect},
 * CR 603.7a), which keeps the original stack entry to put back onto the stack once the last counter is removed.</p>
 */
public record ExileTargetSpellWithDelayCountersEffect() implements CardEffect {

    @Override public TargetSpec targetSpec() { return TargetSpec.harmful(TargetPredicates.spellOnStack()); }
}
