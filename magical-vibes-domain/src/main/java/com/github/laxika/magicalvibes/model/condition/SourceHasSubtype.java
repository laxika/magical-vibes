package com.github.laxika.magicalvibes.model.condition;

import com.github.laxika.magicalvibes.model.CardSubtype;

/** The source permanent (or the card on the stack as fallback) has the given subtype. */
public record SourceHasSubtype(CardSubtype subtype) implements Condition {

    @Override
    public String conditionName() {
        return "source is " + subtype.name().toLowerCase();
    }

    @Override
    public String conditionNotMetReason() {
        return "source is not " + subtype.name().toLowerCase();
    }
}
