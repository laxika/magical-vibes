package com.github.laxika.magicalvibes.model.condition;

import com.github.laxika.magicalvibes.model.CardColor;

public record SourceHasColor(CardColor color) implements Condition {

    @Override
    public String conditionName() {
        return "source is " + color.name().toLowerCase();
    }

    @Override
    public String conditionNotMetReason() {
        return "source is not " + color.name().toLowerCase();
    }
}
