package com.github.laxika.magicalvibes.model.effect;

import java.util.Objects;
import java.util.Set;

/** Checks off one persistent objective on the source permanent and completes the checklist when all are checked. */
public record CompleteChecklistObjectiveEffect(
        String objective,
        Set<String> objectives,
        CardEffect completionEffect
) implements CardEffect {

    public CompleteChecklistObjectiveEffect {
        if (objective == null || objective.isBlank()) {
            throw new IllegalArgumentException("Checklist objective must not be blank");
        }
        objectives = Set.copyOf(Objects.requireNonNull(objectives, "objectives"));
        if (!objectives.contains(objective)) {
            throw new IllegalArgumentException("Checklist objectives must contain the current objective");
        }
        Objects.requireNonNull(completionEffect, "completionEffect");
    }
}
