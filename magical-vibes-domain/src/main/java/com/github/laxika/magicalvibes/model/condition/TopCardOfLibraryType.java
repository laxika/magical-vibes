package com.github.laxika.magicalvibes.model.condition;

import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.effect.LibraryOwner;

/** The top card of the selected library has the given card type. */
public record TopCardOfLibraryType(CardType cardType, LibraryOwner libraryOwner) implements Condition {

    /** Checks the controller's library. */
    public TopCardOfLibraryType(CardType cardType) {
        this(cardType, LibraryOwner.CONTROLLER);
    }

    @Override
    public String conditionName() {
        return "top card of library is a " + cardType.name().toLowerCase();
    }

    @Override
    public String conditionNotMetReason() {
        return "top card of library is not a " + cardType.name().toLowerCase();
    }
}
