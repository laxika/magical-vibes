package com.github.laxika.magicalvibes.model.effect;

/** On resolution, secretly chooses land or nonland, then seeks the chosen kind of card. */
public record SeekChosenLandOrNonlandEffect(int count) implements CardEffect {

    public SeekChosenLandOrNonlandEffect {
        if (count < 0) {
            throw new IllegalArgumentException("count cannot be negative");
        }
    }
}
