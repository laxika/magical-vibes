package com.github.laxika.magicalvibes.model.condition;

/**
 * The controller declared at least {@code minimum} attacking creatures this turn,
 * counted cumulatively across every combat phase of the turn (tracked via
 * {@code gameData.creaturesAttackedCountThisTurn}). Used by Windbrisk Heights.
 */
public record AttackedWithCreaturesThisTurn(int minimum) implements Condition {

    @Override
    public String conditionName() {
        return "attacked with " + minimum + " or more creatures this turn";
    }

    @Override
    public String conditionNotMetReason() {
        return "you didn't attack with " + minimum + " or more creatures this turn";
    }
}
