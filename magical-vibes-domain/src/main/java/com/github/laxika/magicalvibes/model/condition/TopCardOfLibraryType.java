package com.github.laxika.magicalvibes.model.condition;

import com.github.laxika.magicalvibes.model.CardType;

/** The top card of the controller's library has the given card type. */
public record TopCardOfLibraryType(CardType cardType) implements Condition {

    @Override
    public String conditionName() {
        return "top card of library is a " + cardType.name().toLowerCase();
    }

    @Override
    public String conditionNotMetReason() {
        return "top card of library is not a " + cardType.name().toLowerCase();
    }
}
