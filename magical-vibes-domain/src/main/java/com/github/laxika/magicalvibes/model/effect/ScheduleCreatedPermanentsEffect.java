package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.action.DelayedPermanentActionKind;

/** Schedules an action for each permanent created earlier during this resolution. */
public record ScheduleCreatedPermanentsEffect(DelayedPermanentActionKind action,
                                              boolean controllerOnly) implements CardEffect {

    public ScheduleCreatedPermanentsEffect(DelayedPermanentActionKind action) {
        this(action, false);
    }

    /** Schedules the action only at the resolving controller's matching upkeep or end step. */
    public static ScheduleCreatedPermanentsEffect forController(DelayedPermanentActionKind action) {
        return new ScheduleCreatedPermanentsEffect(action, true);
    }
}
