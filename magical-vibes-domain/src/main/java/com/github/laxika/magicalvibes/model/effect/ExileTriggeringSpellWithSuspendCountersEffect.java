package com.github.laxika.magicalvibes.model.effect;

/** Exiles the spell that caused the trigger with suspend counters, optionally followed by an effect. */
public record ExileTriggeringSpellWithSuspendCountersEffect(
        int counters,
        CardEffect followUp,
        boolean skipIfCastFromExile
) implements TriggeringSpellReferencingEffect {

    public ExileTriggeringSpellWithSuspendCountersEffect(int counters) {
        this(counters, null, false);
    }

    /** Exiles the triggering spell and then resolves {@code followUp} if exile succeeds. */
    public ExileTriggeringSpellWithSuspendCountersEffect(int counters, CardEffect followUp) {
        this(counters, followUp, false);
    }

    /** Exiles the triggering spell and follows up, but only when it was not cast from exile. */
    public static ExileTriggeringSpellWithSuspendCountersEffect unlessCastFromExile(
            int counters, CardEffect followUp) {
        return new ExileTriggeringSpellWithSuspendCountersEffect(counters, followUp, true);
    }
}
