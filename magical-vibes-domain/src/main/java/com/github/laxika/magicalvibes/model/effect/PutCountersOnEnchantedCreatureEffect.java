package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CounterType;

/**
 * Puts {@code amount} counters of {@code counterType} on the creature the source Aura is attached
 * to (Krasis Incubation: "{1}{G}{U}, Return this Aura to its owner's hand: Put two +1/+1 counters
 * on enchanted creature").
 *
 * <p>{@link AttachedPermanentSelfTargetingEffect} plus a self-targeting spec means the attached
 * creature is captured onto the stack entry at activation, before costs are paid — the counters
 * still land even though the bounce cost detaches the Aura first.
 */
public record PutCountersOnEnchantedCreatureEffect(CounterType counterType, int amount)
        implements AttachedPermanentSelfTargetingEffect {

    @Override
    public TargetSpec targetSpec() {
        return new TargetSpec(null, false, null, true, 1);
    }
}
