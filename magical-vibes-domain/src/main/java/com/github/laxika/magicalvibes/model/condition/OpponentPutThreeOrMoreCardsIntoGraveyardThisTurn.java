package com.github.laxika.magicalvibes.model.condition;

/** At least one opponent had three or more cards put into their graveyard from anywhere this turn. */
public record OpponentPutThreeOrMoreCardsIntoGraveyardThisTurn() implements Condition {

    @Override
    public String conditionName() {
        return "an opponent had three or more cards put into their graveyard this turn";
    }

    @Override
    public String conditionNotMetReason() {
        return "no opponent had three or more cards put into their graveyard this turn";
    }
}
