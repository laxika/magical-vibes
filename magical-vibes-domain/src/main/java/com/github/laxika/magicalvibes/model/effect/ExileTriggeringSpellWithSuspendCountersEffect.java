package com.github.laxika.magicalvibes.model.effect;

/** Exiles the spell that caused the trigger with the specified number of suspend counters. */
public record ExileTriggeringSpellWithSuspendCountersEffect(int counters)
        implements TriggeringSpellReferencingEffect {
}
