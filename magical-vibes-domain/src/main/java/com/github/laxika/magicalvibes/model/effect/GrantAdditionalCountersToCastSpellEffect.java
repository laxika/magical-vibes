package com.github.laxika.magicalvibes.model.effect;

/**
 * Grants additional +1/+1 counters to the creature spell that caused the triggering ability.
 * The normal-effect handler uses the triggering spell's cast-time colored-mana snapshot as the
 * amount and records the grant for that spell's battlefield entry.
 */
public record GrantAdditionalCountersToCastSpellEffect() implements CardEffect {
}
