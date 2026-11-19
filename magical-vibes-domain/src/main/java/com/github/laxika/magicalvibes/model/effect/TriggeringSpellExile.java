package com.github.laxika.magicalvibes.model.effect;

/**
 * How an {@link ExileTriggeringSpellEffect} exiles the triggering spell instead of letting it go to the
 * graveyard as it resolves.
 */
public enum TriggeringSpellExile {
    /** Exiled and tracked as exiled with the triggered ability's source (Rod of Absorption, Forger's Foundry). */
    TRACKED_BY_SOURCE,
    /** Exiled with a dream counter on it (Goliath Daydreamer). */
    DREAM_COUNTER,
    /** Exiled and plotted (Lilah, Undefeated Slickshot). */
    PLOTTED,
    /** Exiled with suspend time counters on it (Gandalf of the Secret Fire). */
    SUSPENDED,
    /** Exiled and returned to its owner's hand at the beginning of the next end step (Feather, the Redeemed). */
    RETURN_TO_HAND_AT_NEXT_END_STEP
}
