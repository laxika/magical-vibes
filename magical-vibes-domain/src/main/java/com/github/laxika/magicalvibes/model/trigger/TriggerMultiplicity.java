package com.github.laxika.magicalvibes.model.trigger;

/** How many times one event triggers an ability when several of the event's occurrences match it. */
public enum TriggerMultiplicity {
    /**
     * Once for each matching occurrence (CR 603.2c): "Whenever a land is put into a graveyard from
     * the battlefield" triggers once for each land a spell destroys. An event with one occurrence,
     * such as a step beginning, triggers it once.
     */
    EACH,
    /** Once for the event however many of its occurrences match: "Whenever one or more creatures die". */
    ONE_OR_MORE
}
