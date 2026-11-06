package com.github.laxika.magicalvibes.model.effect;

/**
 * Chooses a creature card in the controller's hand and conjures a duplicate into that hand.
 *
 * <p>The effect first asks for the card, with {@link #afterChoice()} as the choice's follow-up; that step reads the
 * card from the stack entry ({@code StackEntry.getChosenObjectCard()}).
 *
 * @param cardChosen whether the card is already chosen (the step after the choice)
 */
public record ChooseCreatureCardFromHandAndConjureDuplicateIntoHandEffect(boolean cardChosen)
        implements CardEffect {

    public ChooseCreatureCardFromHandAndConjureDuplicateIntoHandEffect() {
        this(false);
    }

    /** The step after the card is chosen: it reads the stack entry's chosen card. */
    public static ChooseCreatureCardFromHandAndConjureDuplicateIntoHandEffect afterChoice() {
        return new ChooseCreatureCardFromHandAndConjureDuplicateIntoHandEffect(true);
    }
}
