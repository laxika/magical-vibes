package com.github.laxika.magicalvibes.model.condition;

/** At least {@code minimum} creature cards were put into graveyards from anywhere this turn. */
public record CreatureCardsPutIntoGraveyardThisTurnAtLeast(int minimum) implements Condition {

    @Override
    public String conditionName() {
        return minimum == 1
                ? "a creature card was put into a graveyard from anywhere this turn"
                : minimum + " or more creature cards were put into graveyards from anywhere this turn";
    }

    @Override
    public String conditionNotMetReason() {
        return "fewer than " + minimum + " creature cards were put into graveyards from anywhere this turn";
    }
}
