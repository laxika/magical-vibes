package com.github.laxika.magicalvibes.model.condition;

import com.github.laxika.magicalvibes.model.ManaColor;

/** The controller's devotion to {@code color} is at least the number of cards in their library. */
public record DevotionToColorAtLeastCardsInLibrary(ManaColor color) implements Condition {

    @Override
    public String conditionName() {
        return "devotion to " + color.name().toLowerCase() + " is at least the number of cards in library";
    }

    @Override
    public String conditionNotMetReason() {
        return "devotion to " + color.name().toLowerCase() + " is less than the number of cards in library";
    }
}
