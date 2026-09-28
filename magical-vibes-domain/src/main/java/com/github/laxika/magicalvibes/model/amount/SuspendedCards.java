package com.github.laxika.magicalvibes.model.amount;

/** The number of face-up cards with suspend time counters in the exile zones in scope. */
public record SuspendedCards(CountScope scope) implements DynamicAmount {

    public SuspendedCards() {
        this(CountScope.CONTROLLER);
    }
}
