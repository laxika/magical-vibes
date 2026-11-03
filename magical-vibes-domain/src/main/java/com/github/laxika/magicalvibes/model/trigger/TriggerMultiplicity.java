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
     * Once for each creature blocking the attacking creature: "whenever this creature becomes blocked by a creature"
     * (CR 509.3d), where the default "becomes blocked" triggers once however many creatures block it (CR 509.3c).
     */
    EACH_BLOCKER
}
