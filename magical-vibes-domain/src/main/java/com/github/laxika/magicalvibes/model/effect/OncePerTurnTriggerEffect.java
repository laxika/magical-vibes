package com.github.laxika.magicalvibes.model.effect;

/**
 * Wrapper for triggered abilities that "trigger only once each turn" (e.g. Ghoulish Procession).
 * The engine marks the source permanent or graveyard card when the wrapped ability first fires in
 * a turn and skips subsequent events for that source until the turn clears. A keyed wrapper can
 * track multiple independent once-per-turn abilities on the same source.
 */
public record OncePerTurnTriggerEffect(CardEffect wrapped, boolean markOnAcceptance, String key) implements CardEffect {

    @Override
    public TargetSpec targetSpec() {
        return wrapped.targetSpec();
    }

    public OncePerTurnTriggerEffect(CardEffect wrapped) {
        this(wrapped, false, null);
    }

    public OncePerTurnTriggerEffect(CardEffect wrapped, boolean markOnAcceptance) {
        this(wrapped, markOnAcceptance, null);
    }

    public OncePerTurnTriggerEffect(CardEffect wrapped, String key) {
        this(wrapped, false, key);
    }

    public static OncePerTurnTriggerEffect markOnAcceptance(CardEffect wrapped) {
        return new OncePerTurnTriggerEffect(wrapped, true, null);
    }

    public static OncePerTurnTriggerEffect keyed(CardEffect wrapped, String key) {
        return new OncePerTurnTriggerEffect(wrapped, false, key);
    }
}
