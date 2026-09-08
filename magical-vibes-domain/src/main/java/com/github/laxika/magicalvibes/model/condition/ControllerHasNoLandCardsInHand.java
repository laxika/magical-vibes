package com.github.laxika.magicalvibes.model.condition;

/**
 * The controller has no land cards in hand. Used by alternate casting costs such as Land Grant.
 */
public record ControllerHasNoLandCardsInHand() implements Condition {

    @Override
    public String conditionName() {
        return "no land cards in hand";
    }

    @Override
    public String conditionNotMetReason() {
        return "you have a land card in hand";
    }
}
