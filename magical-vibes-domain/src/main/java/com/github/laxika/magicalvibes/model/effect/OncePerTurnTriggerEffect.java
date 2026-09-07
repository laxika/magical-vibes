package com.github.laxika.magicalvibes.model.effect;

/**
 * Wrapper for triggered abilities that "trigger only once each turn" (e.g. Ghoulish Procession).
 * The engine marks the source permanent or graveyard card when the wrapped ability first fires in
 * a turn and skips subsequent events for that source until the turn clears.
 */
public record OncePerTurnTriggerEffect(CardEffect wrapped, boolean markOnAcceptance) implements CardEffect {

    public OncePerTurnTriggerEffect(CardEffect wrapped) {
        this(wrapped, false);
    }

    public static OncePerTurnTriggerEffect markOnAcceptance(CardEffect wrapped) {
        return new OncePerTurnTriggerEffect(wrapped, true);
    }
}
