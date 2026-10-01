package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.amount.DynamicAmount;

/**
 * Grants additional +1/+1 counters to the creature spell that caused the triggering ability.
 * The no-argument form uses the triggering spell's cast-time colored-mana snapshot as the amount;
 * the dynamic form evaluates its amount relative to the triggering ability's source.
 */
public record GrantAdditionalCountersToCastSpellEffect(DynamicAmount amount) implements CardEffect {

    public GrantAdditionalCountersToCastSpellEffect() {
        this(null);
    }
}
