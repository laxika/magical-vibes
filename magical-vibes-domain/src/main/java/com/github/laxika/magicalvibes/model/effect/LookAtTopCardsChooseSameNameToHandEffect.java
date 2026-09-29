package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.Card;

import java.util.List;

/**
 * Looks at the top cards of the controller's library, chooses one, and puts every looked-at card
 * with the chosen card's name into hand. The remaining cards are put on the bottom in random order
 * and the controller loses one life for each card put into hand this way.
 *
 * <p>The list-bearing form is an internal continuation used after the interactive card choice.
 *
 * @param count number of cards to look at for the initial resolution
 * @param lookedAtCards cards retained for the post-choice continuation
 */
public record LookAtTopCardsChooseSameNameToHandEffect(int count, List<Card> lookedAtCards)
        implements CardEffect {

    public LookAtTopCardsChooseSameNameToHandEffect(int count) {
        this(count, List.of());
    }

    public LookAtTopCardsChooseSameNameToHandEffect {
        lookedAtCards = List.copyOf(lookedAtCards);
    }

    public static LookAtTopCardsChooseSameNameToHandEffect continuation(List<Card> lookedAtCards) {
        return new LookAtTopCardsChooseSameNameToHandEffect(0, lookedAtCards);
    }
}
