package com.github.laxika.magicalvibes.model.trigger;

/** How many times one event triggers an ability when several of the event's occurrences match it. */
public enum TriggerMultiplicity {
    /**
     * Once for each matching occurrence (CR 603.2c): "Whenever a land is put into a graveyard from
     * the battlefield" triggers once for each land a spell destroys. An event with one occurrence,
     * such as a step beginning, triggers it once.
     */
    EACH,
    /**
     * Once for the event however many of its occurrences match: "Whenever one or more creatures die". For "whenever
     * this creature blocks" it is once each combat however many creatures it blocks (CR 509.3a), where the default
     * is once for each creature it blocks ("blocks a creature", CR 509.3b).
     */
    ONE_OR_MORE,
    /**
     * Once for the combat damage step however many players the damage is dealt to: "whenever one or more creatures
     * you control deal combat damage to one or more players" (Forth Eorlingas!), where {@link #ONE_OR_MORE} on "to a
     * player" is once for each player dealt damage (CR 603.2c). Only delayed "deals combat damage to a player"
     * triggers have it.
     */
    ONE_OR_MORE_PLAYERS,
    /**
     * Once for each creature blocking the attacking creature: "whenever this creature becomes blocked by a creature"
     * (CR 509.3d), where the default "becomes blocked" triggers once however many creatures block it (CR 509.3c).
     */
    EACH_BLOCKER,
    /**
     * Once for each source that deals damage in the event: "whenever a source deals damage to this creature" (Nested
     * Ghoul), where combat damage from several creatures at once is several occurrences of one event (CR 603.2c).
     * Only damage triggers have it.
     */
    EACH_SOURCE,
    /**
     * Once for each band of two or more of the attacking creatures declared (CR 702.22c): "whenever two or more
     * creatures you control attack in a band" (Chatzuk, Mighty Guitarist), where the band's creatures are the
     * ability's attacking creatures. Only "whenever you attack" has it.
     */
    EACH_BAND
}
