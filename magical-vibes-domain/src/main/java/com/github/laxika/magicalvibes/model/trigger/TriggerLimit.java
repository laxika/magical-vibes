package com.github.laxika.magicalvibes.model.trigger;

/** A limit on how often a triggered ability triggers, from the sentence that follows it on the card. */
public enum TriggerLimit {
    NONE,
    /** "This ability triggers only once each turn." */
    ONCE_EACH_TURN,
    /**
     * "Do this only once each turn" after an instruction: the ability triggers only if its source's
     * controller hasn't yet taken that action this turn (CR 603.2h), so declining a "you may" leaves
     * it free to trigger again that turn.
     */
    DO_ONLY_ONCE_EACH_TURN,
    /** "This ability triggers only once." */
    ONCE
}
