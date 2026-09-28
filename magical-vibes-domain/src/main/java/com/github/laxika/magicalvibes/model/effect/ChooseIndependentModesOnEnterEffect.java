package com.github.laxika.magicalvibes.model.effect;

import java.util.List;

/**
 * Marker for an as-enters choice that requires one mode from each independent group.
 *
 * <p>Unlike {@link ChooseModeOnEnterEffect}, modes from different groups are not alternatives to
 * one another. The entry-choice pipeline presents the groups one at a time and records all chosen
 * labels on the entering permanent.</p>
 */
public record ChooseIndependentModesOnEnterEffect(List<List<String>> modeGroups) implements CardEffect {

    public ChooseIndependentModesOnEnterEffect {
        if (modeGroups == null || modeGroups.isEmpty() || modeGroups.stream()
                .anyMatch(group -> group == null || group.isEmpty())) {
            throw new IllegalArgumentException("At least one non-empty as-enters mode group is required");
        }
        modeGroups = modeGroups.stream().map(List::copyOf).toList();
    }
}
