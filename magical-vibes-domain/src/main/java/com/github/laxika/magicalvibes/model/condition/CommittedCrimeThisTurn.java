package com.github.laxika.magicalvibes.model.condition;

/**
 * True if the effect's controller targeted an opponent, something an opponent controls, or a card
 * in an opponent's graveyard this turn (CR 700.13).
 */
public record CommittedCrimeThisTurn() implements Condition {

    @Override
    public String conditionName() {
        return "committed a crime";
    }

    @Override
    public String conditionNotMetReason() {
        return "you haven't committed a crime this turn";
    }
}
