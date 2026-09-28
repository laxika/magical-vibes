package com.github.laxika.magicalvibes.model.condition;

/** The controller cycled at least {@code minimum} cards this turn. */
public record ControllerCycledAtLeastCardsThisTurn(int minimum) implements Condition {

    public ControllerCycledAtLeastCardsThisTurn {
        if (minimum < 1) {
            throw new IllegalArgumentException("minimum must be positive");
        }
    }

    @Override
    public String conditionName() {
        return "you cycled " + minimum + " or more cards this turn";
    }

    @Override
    public String conditionNotMetReason() {
        return "you cycled fewer than " + minimum + " cards this turn";
    }
}
