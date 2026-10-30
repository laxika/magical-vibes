package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.amount.DynamicAmount;

/**
 * Grants additional +1/+1 counters to the creature spell that caused the resolving spell-cast
 * trigger. The amount is evaluated when this effect resolves, or, when {@code countedOnEntry} is
 * set, when the creature enters the battlefield (Torgal, A Fine Hound).
 */
public record GrantAdditionalPlusOnePlusOneCountersToTriggeringCreatureSpellEffect(
        DynamicAmount amount, boolean countedOnEntry) implements TriggeringSpellReferencingEffect {

    public GrantAdditionalPlusOnePlusOneCountersToTriggeringCreatureSpellEffect(DynamicAmount amount) {
        this(amount, false);
    }
}
