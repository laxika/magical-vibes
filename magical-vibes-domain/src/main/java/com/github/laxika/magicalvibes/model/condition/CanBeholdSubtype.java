package com.github.laxika.magicalvibes.model.condition;

import com.github.laxika.magicalvibes.model.CardSubtype;

/** The controller can behold a matching permanent or card in hand. */
public record CanBeholdSubtype(CardSubtype subtype) implements Condition {

    @Override
    public String conditionName() {
        return "can behold a " + subtype.name().toLowerCase();
    }

    @Override
    public String conditionNotMetReason() {
        return "controller cannot behold a " + subtype.name().toLowerCase();
    }
}
