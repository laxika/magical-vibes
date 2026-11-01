package com.github.laxika.magicalvibes.model.rules;

import java.util.List;

/**
 * The rules events of one occurrence, such as every permanent a wrath destroys or every card one mill
 * moves. "One or more" triggers and abilities that trigger once per occurrence read a batch (CR 603.2c).
 *
 * @param id     identifies the batch within its game, in emission order
 * @param events the events, in the order they happened; never empty
 */
public record RulesEventBatch(long id, List<RulesEvent> events) {

    public RulesEventBatch {
        events = List.copyOf(events);
        if (events.isEmpty()) {
            throw new IllegalArgumentException("A rules event batch holds at least one event");
        }
    }
}
