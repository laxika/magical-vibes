package com.github.laxika.magicalvibes.model.condition;

/**
 * The controller's starting deck had at least {@code cardsOverMinimum} cards above the
 * format's minimum deck size.
 */
public record StartingDeckAtLeast(int minimumDeckSize, int cardsOverMinimum) implements Condition {

    @Override
    public String conditionName() {
        return "starting deck at least " + (minimumDeckSize + cardsOverMinimum) + " cards";
    }

    @Override
    public String conditionNotMetReason() {
        return "starting deck had fewer than " + (minimumDeckSize + cardsOverMinimum) + " cards";
    }
}
