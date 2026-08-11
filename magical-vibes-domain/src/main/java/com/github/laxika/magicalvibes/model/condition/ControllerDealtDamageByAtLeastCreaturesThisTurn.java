package com.github.laxika.magicalvibes.model.condition;

/** The controller was dealt damage by at least the given number of distinct creatures this turn. */
public record ControllerDealtDamageByAtLeastCreaturesThisTurn(int minimumCreatures) implements Condition {

    @Override
    public String conditionName() {
        return "you were dealt damage by " + minimumCreatures + " or more creatures this turn";
    }

    @Override
    public String conditionNotMetReason() {
        return "you were not dealt damage by " + minimumCreatures + " or more creatures this turn";
    }
}
