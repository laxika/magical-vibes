package com.github.laxika.magicalvibes.model.condition;

/**
 * True when at least one opponent of the controller has no cards in hand ("if an opponent has no
 * cards in hand"). Evaluated against the controller's opponents (Rekindled Flame's graveyard
 * upkeep intervening-"if"). Distinct from {@link NoPlayerHasCardsInHand}, which requires every
 * player's hand to be empty.
 */
public record AnOpponentHandEmpty() implements Condition {

    @Override
    public String conditionName() {
        return "an opponent has no cards in hand";
    }

    @Override
    public String conditionNotMetReason() {
        return "every opponent still has cards in hand";
    }
}
