package com.github.laxika.magicalvibes.model.condition;

import com.github.laxika.magicalvibes.model.effect.LibraryOwner;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;

/** The top card of the selected library matches the given card predicate. */
public record TopCardOfLibraryMatchesPredicate(CardPredicate predicate, LibraryOwner libraryOwner)
        implements Condition {

    /** Checks the controller's library. */
    public TopCardOfLibraryMatchesPredicate(CardPredicate predicate) {
        this(predicate, LibraryOwner.CONTROLLER);
    }

    @Override
    public String conditionName() {
        return "top card of library matches the required predicate";
    }

    @Override
    public String conditionNotMetReason() {
        return "top card of library does not match the required predicate";
    }
}
