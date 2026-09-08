package com.github.laxika.magicalvibes.model.amount;

/** The number of cards drawn this turn by the players in scope. */
public record CardsDrawnThisTurn(CountScope scope) implements DynamicAmount {

    public CardsDrawnThisTurn() {
        this(CountScope.CONTROLLER);
    }
}
