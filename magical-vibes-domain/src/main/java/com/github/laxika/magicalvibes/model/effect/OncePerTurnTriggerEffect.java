package com.github.laxika.magicalvibes.model.effect;

/**
 * Wrapper for triggered abilities that "trigger only once each turn" (e.g. Ghoulish Procession).
 * The engine marks the source permanent or graveyard card when the wrapped ability first fires in
 * a turn and skips subsequent events for that source until the turn clears. A keyed wrapper can
 * track multiple independent once-per-turn abilities on the same source.
 */
public record OncePerTurnTriggerEffect(CardEffect wrapped, boolean markOnAcceptance, String key,
                                      boolean firstTokenCreation, boolean firstLifeGain,
                                      boolean firstLandGraveyardEvent) implements CardEffect {

    public OncePerTurnTriggerEffect(CardEffect wrapped, boolean markOnAcceptance, String key,
                                   boolean firstTokenCreation, boolean firstLifeGain) {
        this(wrapped, markOnAcceptance, key, firstTokenCreation, firstLifeGain, false);
    }

    /** Includes land-graveyard events that occurred before the source entered. */
    public static OncePerTurnTriggerEffect firstLandGraveyardEvent(CardEffect wrapped) {
        return new OncePerTurnTriggerEffect(wrapped, false, null, false, false, true);
    }

    public OncePerTurnTriggerEffect(CardEffect wrapped, boolean markOnAcceptance, String key,
                                   boolean firstTokenCreation) {
        this(wrapped, markOnAcceptance, key, firstTokenCreation, false);
    }

    public OncePerTurnTriggerEffect(CardEffect wrapped, boolean markOnAcceptance, String key) {
        this(wrapped, markOnAcceptance, key, false);
    }

    /** Triggers for the controller's first token creation event, including events before entry. */
    public static OncePerTurnTriggerEffect firstTokenCreation(CardEffect wrapped) {
        return new OncePerTurnTriggerEffect(wrapped, false, null, true);
    }

    /** Triggers for the controller's first life gain event, including events before entry. */
    public static OncePerTurnTriggerEffect firstLifeGain(CardEffect wrapped) {
        return new OncePerTurnTriggerEffect(wrapped, false, null, false, true);
    }

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
