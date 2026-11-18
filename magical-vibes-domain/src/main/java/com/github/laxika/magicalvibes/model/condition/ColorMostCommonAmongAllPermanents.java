package com.github.laxika.magicalvibes.model.condition;

import com.github.laxika.magicalvibes.model.CardColor;

/** True when the given color is tied for the most common color among all permanents. */
public record ColorMostCommonAmongAllPermanents(CardColor color) implements Condition {

    @Override
    public String conditionName() {
        return color.name().toLowerCase() + " is most common among all permanents or tied";
    }

    @Override
    public String conditionNotMetReason() {
        return color.name().toLowerCase() + " is not most common among all permanents and is not tied";
    }
}
