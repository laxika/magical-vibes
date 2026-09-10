package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.action.DelayedPermanentActionKind;

/** Schedules an action for each permanent created earlier during this resolution. */
public record ScheduleCreatedPermanentsEffect(DelayedPermanentActionKind action) implements CardEffect {
}
