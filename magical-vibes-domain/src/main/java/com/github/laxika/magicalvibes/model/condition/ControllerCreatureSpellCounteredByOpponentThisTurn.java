package com.github.laxika.magicalvibes.model.condition;

/** A creature spell cast by the controller this turn was countered by an opponent. */
public record ControllerCreatureSpellCounteredByOpponentThisTurn() implements Condition {

    @Override
    public String conditionName() {
        return "a creature spell you cast this turn was countered by an opponent";
    }

    @Override
    public String conditionNotMetReason() {
        return "no creature spell you cast this turn was countered by an opponent";
    }
}
