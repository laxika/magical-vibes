package com.github.laxika.magicalvibes.model.effect;

/**
 * Exiles the spell that caused the trigger with suspend counters, optionally followed by an effect.
 * The follow-up still resolves when the spell is no longer on the stack to exile (Rory Williams:
 * "exile it ... Then investigate"). A condition on the cast ("from anywhere other than exile", Rory Williams) is the
 * ability's event condition, {@code TriggerOptions.withEventCondition(new NotCondition(new CastFromZone(Zone.EXILE)))}.
 */
public record ExileTriggeringSpellWithSuspendCountersEffect(
        int counters,
        CardEffect followUp
) implements CardEffect {

    public ExileTriggeringSpellWithSuspendCountersEffect(int counters) {
        this(counters, null);
    }
}
