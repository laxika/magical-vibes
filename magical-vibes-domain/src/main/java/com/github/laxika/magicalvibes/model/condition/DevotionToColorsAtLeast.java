package com.github.laxika.magicalvibes.model.condition;

import com.github.laxika.magicalvibes.model.ManaColor;

import java.util.Set;

/** The controller's combined devotion to the given colors is at least {@code threshold}. */
public record DevotionToColorsAtLeast(Set<ManaColor> colors, int threshold) implements Condition {

    public DevotionToColorsAtLeast {
        colors = Set.copyOf(colors);
    }

    @Override
    public String conditionName() {
        return "devotion to " + colors.stream()
                .map(color -> color.name().toLowerCase())
                .sorted()
                .reduce((left, right) -> left + " and " + right)
                .orElse("the given colors") + " is at least " + threshold;
    }

    @Override
    public String conditionNotMetReason() {
        return "devotion to the given colors is less than " + threshold;
    }
}
