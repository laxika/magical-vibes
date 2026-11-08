package com.github.laxika.magicalvibes.model.effect;

/**
 * "Put a +1/+1 counter on that creature if its power is less than this creature's power. Otherwise, put a +1/+1
 * counter on this creature" (Shelinda): compares the power of the stack entry's triggering permanent with the
 * source's as the ability resolves. A branch that affects "that creature" reads the triggering permanent
 * ({@code PutCounterOnTargetPermanentEffect.onTriggeringPermanent}).
 */
public record EnteringCreatureSourcePowerBranchEffect(
        CardEffect sourcePowerAtLeast,
        CardEffect belowSourcePower
) implements CardEffect {
}
