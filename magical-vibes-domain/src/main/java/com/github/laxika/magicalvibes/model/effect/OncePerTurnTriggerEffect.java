package com.github.laxika.magicalvibes.model.effect;

/**
 * Wrapper for triggered abilities that "trigger only once each turn" (e.g. Ghoulish Procession).
 * The engine marks the source permanent or graveyard card when the wrapped ability first fires in
 * a turn and skips subsequent events for that source until the turn clears. A keyed wrapper can
 * track multiple independent once-per-turn abilities on the same source.
 */
public record OncePerTurnTriggerEffect(CardEffect wrapped, boolean markOnAcceptance, String key,
                                      boolean firstTokenCreation, boolean firstLifeGain,
                                      boolean firstLandGraveyardEvent, boolean firstLifeLoss) implements CardEffect {

    public OncePerTurnTriggerEffect(CardEffect wrapped, boolean markOnAcceptance, String key,
                                   boolean firstTokenCreation, boolean firstLifeGain,
                                   boolean firstLandGraveyardEvent) {
        this(wrapped, markOnAcceptance, key, firstTokenCreation, firstLifeGain, firstLandGraveyardEvent, false);
    }

    public OncePerTurnTriggerEffect(CardEffect wrapped, boolean markOnAcceptance, String key,
                                   boolean firstTokenCreation, boolean firstLifeGain) {
        this(wrapped, markOnAcceptance, key, firstTokenCreation, firstLifeGain, false, false);
    }

    /**
     * Triggers only for the controller's first life loss event each turn, counting events before the
     * source entered. The limit belongs to the controller rather than the source, so every copy
     * present for that first event triggers and no copy triggers on later events (Gonti's Machinations).
     */
    public static OncePerTurnTriggerEffect firstLifeLoss(CardEffect wrapped) {
        return new OncePerTurnTriggerEffect(wrapped, false, null, false, false, false, true);
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

    /** Placeholder key that {@code GrantTriggeredAbilityEffect} replaces with one key per granting permanent. */
    public static final String PER_GRANT_KEY = "per-grant";

    /** Key marking an ability that triggers only for its controller's first discard event each turn. */
    public static final String FIRST_DISCARD_EVENT_KEY = "first-discard-event";

    /**
     * Triggers only for the controller's first discard event each turn, counting discards made before
     * the source entered (Rielle, the Everwise).
     */
    public static OncePerTurnTriggerEffect firstDiscardEvent(CardEffect wrapped) {
        return new OncePerTurnTriggerEffect(wrapped, false, FIRST_DISCARD_EVENT_KEY);
    }

    /**
     * For a granted ability: each granting permanent gives its own copy of the ability, so each has
     * its own once-per-turn limit on the receiving permanent. Only valid on slots whose collector
     * marks through {@code OncePerTurnTriggerSupport} (keyed marks).
     */
    public static OncePerTurnTriggerEffect perGrant(CardEffect wrapped) {
        return new OncePerTurnTriggerEffect(wrapped, false, PER_GRANT_KEY);
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
