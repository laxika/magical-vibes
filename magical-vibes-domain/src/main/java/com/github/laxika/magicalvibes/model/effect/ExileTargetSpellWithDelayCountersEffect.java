package com.github.laxika.magicalvibes.model.effect;

/**
 * "Target spell's controller exiles it with X delay counters on it." (Ertai's Meddling)
 *
 * <p>X comes from the stack entry's X value. The spell leaves the stack without being countered —
 * "can't be countered" does not stop this — and the card is exiled with a record of the original
 * stack entry so it can be put back onto the stack once its last delay counter is removed by
 * {@link RemoveDelayCounterFromExiledSpellEffect}.</p>
 */
public record ExileTargetSpellWithDelayCountersEffect() implements CardEffect {

    @Override public TargetSpec targetSpec() { return TargetSpec.harmful(TargetCategory.SPELL_ON_STACK); }
}
