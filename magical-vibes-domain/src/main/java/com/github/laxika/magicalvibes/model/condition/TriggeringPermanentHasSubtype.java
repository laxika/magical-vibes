package com.github.laxika.magicalvibes.model.condition;

import com.github.laxika.magicalvibes.model.CardSubtype;

/** The permanent that caused the trigger has the given subtype. */
public record TriggeringPermanentHasSubtype(CardSubtype subtype) implements Condition {

    @Override
    public String conditionName() {
        return "triggering permanent is a " + subtype.name().toLowerCase();
    }

    @Override
    public String conditionNotMetReason() {
        return "triggering permanent is not a " + subtype.name().toLowerCase();
    }
}
