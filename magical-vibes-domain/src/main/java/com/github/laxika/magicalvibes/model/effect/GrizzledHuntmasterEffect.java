package com.github.laxika.magicalvibes.model.effect;

/**
 * Resolves Grizzled Huntmaster's optional hand exile, same-name exile, and conjure sequence, one {@link Step} at a
 * time. Each choice's follow-up is the next step; the steps after a card choice read that card from the stack entry
 * ({@code StackEntry.getChosenObjectCard()}), and the step after the same-name exile reads how many cards it exiled
 * from the entry's event value.
 *
 * @param step            the step to resolve
 * @param cardName        the name of the creature card exiled from hand (from {@link Step#SAME_NAME_CARDS_EXILED})
 * @param handExiledCount the number of copies to conjure, once known
 */
public record GrizzledHuntmasterEffect(Step step, String cardName, int handExiledCount) implements CardEffect {

    /** The steps of the ability, each the follow-up of the choice before it. */
    public enum Step {
        /** Reveal the hand and choose a creature card to exile. */
        CHOOSE_HAND_CARD,
        /** The creature card was exiled from hand: exile its same-named cards from hand and library. */
        HAND_CARD_CHOSEN,
        /** The same-named cards were exiled: choose a creature card from outside the game. */
        SAME_NAME_CARDS_EXILED,
        /** The card outside the game was chosen: conjure its duplicates into hand. */
        CONJURE
    }

    public GrizzledHuntmasterEffect() {
        this(Step.CHOOSE_HAND_CARD, null, 0);
    }
}
