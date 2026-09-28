package com.github.laxika.magicalvibes.model.condition;

/** True when the controller has strictly more cards in their library than the targeted player. */
public record ControllerHasMoreCardsInLibraryThanTargetPlayer() implements Condition {

    @Override
    public String conditionName() {
        return "more cards in your library than that player's";
    }

    @Override
    public String conditionNotMetReason() {
        return "your library does not have more cards than that player's";
    }
}
