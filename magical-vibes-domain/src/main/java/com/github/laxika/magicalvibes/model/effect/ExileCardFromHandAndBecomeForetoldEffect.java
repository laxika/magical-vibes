package com.github.laxika.magicalvibes.model.effect;

/**
 * Exiles a chosen card from hand face down and gives it a foretell cost reduced by {2}.
 *
 * <p>The effect first asks for the card, with {@link #afterChoice()} as the choice's follow-up; that step reads the
 * card from the stack entry ({@code StackEntry.getChosenObjectCard()}). A library search follow-up of the
 * {@code afterChoice} form reads the found card the same way.
 *
 * @param cardChosen whether the card is already chosen (the step after the choice)
 */
public record ExileCardFromHandAndBecomeForetoldEffect(boolean cardChosen) implements CardEffect {

    public ExileCardFromHandAndBecomeForetoldEffect() {
        this(false);
    }

    /** The step after the card is chosen: it reads the stack entry's chosen card. */
    public static ExileCardFromHandAndBecomeForetoldEffect afterChoice() {
        return new ExileCardFromHandAndBecomeForetoldEffect(true);
    }
}
