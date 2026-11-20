package com.github.laxika.magicalvibes.model.effect;

/**
 * Exiles the spell that caused the trigger from the stack with {@code counters} time counters; it gains suspend. Nothing
 * is exiled when the spell is no longer on the stack. A "then" after it ("Then investigate", Rory Williams) is the
 * ability's next effect, which resolves either way. A condition on the cast ("from anywhere other than exile", Rory
 * Williams) is the ability's event condition,
 * {@code TriggerOptions.withEventCondition(new NotCondition(new CastFromZone(Zone.EXILE)))}.
 */
public record ExileTriggeringSpellWithSuspendCountersEffect(int counters) implements CardEffect {
}
