package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.amount.DynamicAmount;

/**
 * Grants additional +1/+1 counters to the creature spell that caused the resolving spell-cast
 * trigger. The amount is evaluated when this effect resolves.
 */
public record GrantAdditionalPlusOnePlusOneCountersToTriggeringCreatureSpellEffect(
        DynamicAmount amount) implements CardEffect {
}
