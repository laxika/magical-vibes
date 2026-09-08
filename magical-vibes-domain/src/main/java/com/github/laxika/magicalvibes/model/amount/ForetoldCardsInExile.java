package com.github.laxika.magicalvibes.model.amount;

/** The number of foretold cards owned by players in the given scope and currently in exile. */
public record ForetoldCardsInExile(CountScope scope) implements DynamicAmount {

    public ForetoldCardsInExile() {
        this(CountScope.CONTROLLER);
    }
}
