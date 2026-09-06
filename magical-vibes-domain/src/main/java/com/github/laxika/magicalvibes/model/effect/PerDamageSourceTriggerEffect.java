package com.github.laxika.magicalvibes.model.effect;

/**
 * Marks a dealt-damage trigger that must trigger once for each damage source in an event.
 * The trigger collectors unwrap {@link #effect()} before dispatch and resolution.
 */
public record PerDamageSourceTriggerEffect(CardEffect effect) implements CardEffect {
}
