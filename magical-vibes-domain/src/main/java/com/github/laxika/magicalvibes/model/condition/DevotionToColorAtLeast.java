package com.github.laxika.magicalvibes.model.condition;

import com.github.laxika.magicalvibes.model.ManaColor;

/** The controller's devotion to {@code color} is at least {@code threshold}. */
public record DevotionToColorAtLeast(ManaColor color, int threshold) implements Condition {

    @Override
    public String conditionName() {
        return "devotion to " + color.name().toLowerCase() + " is at least " + threshold;
    }

    @Override
    public String conditionNotMetReason() {
        return "devotion to " + color.name().toLowerCase() + " is less than " + threshold;
    }
}
