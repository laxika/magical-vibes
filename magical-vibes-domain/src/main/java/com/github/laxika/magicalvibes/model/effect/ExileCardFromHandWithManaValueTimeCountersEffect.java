package com.github.laxika.magicalvibes.model.effect;

/**
 * Exiles a chosen hand card, gives it time counters equal to its mana value, then advances other owned cards in exile.
 * When {@code advancesOtherExiledCards} is false the other exiled cards keep their counters. When {@code gainsSuspend}
 * is true the card gains suspend (CR 702.62) instead of being tracked as an Alaundo-style time-counter card.
 *
 * <p>The effect first asks for the card, with {@link #afterChoice()} as the choice's follow-up; that step reads the
 * exiled card from the stack entry ({@code StackEntry.getChosenObjectCard()}).
 *
 * @param cardChosen whether the card is already chosen (the step after the choice)
 */
public record ExileCardFromHandWithManaValueTimeCountersEffect(boolean advancesOtherExiledCards,
                                                               boolean gainsSuspend, boolean cardChosen)
        implements CardEffect {

    public ExileCardFromHandWithManaValueTimeCountersEffect() {
        this(true, false, false);
    }

    public static ExileCardFromHandWithManaValueTimeCountersEffect gainingSuspend() {
        return new ExileCardFromHandWithManaValueTimeCountersEffect(false, true, false);
    }

    /** The step after the card is chosen: it reads the stack entry's chosen card. */
    public ExileCardFromHandWithManaValueTimeCountersEffect afterChoice() {
        return new ExileCardFromHandWithManaValueTimeCountersEffect(advancesOtherExiledCards, gainsSuspend, true);
    }
}
