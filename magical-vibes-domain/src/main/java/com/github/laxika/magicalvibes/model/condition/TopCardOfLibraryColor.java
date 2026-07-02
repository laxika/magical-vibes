package com.github.laxika.magicalvibes.model.condition;

import com.github.laxika.magicalvibes.model.CardColor;

/** The top card of the controller's library is the given color (e.g. Vampire Nocturnus). */
public record TopCardOfLibraryColor(CardColor color) implements Condition {

    @Override
    public String conditionName() {
        return "top card of library is " + color.name().toLowerCase();
    }

    @Override
    public String conditionNotMetReason() {
        return "top card of library is not " + color.name().toLowerCase();
    }
}
