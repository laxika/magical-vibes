package com.github.laxika.magicalvibes.model.condition;

/**
 * The controller's starting deck had at least {@code cardsOverMinimum} cards above the
 * format's minimum deck size (see {@link com.github.laxika.magicalvibes.model.DeckFormat#minimumDeckSize()}).
 */
public record StartingDeckAtLeast(int cardsOverMinimum) implements Condition {

    @Override
    public String conditionName() {
        return "starting deck at least " + cardsOverMinimum + " cards over the minimum deck size";
    }

    @Override
    public String conditionNotMetReason() {
        return "starting deck had fewer than " + cardsOverMinimum + " cards over the minimum deck size";
    }
}
