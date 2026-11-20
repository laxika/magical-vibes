package com.github.laxika.magicalvibes.model.effect;

import java.util.Objects;

/**
 * "Put that card into your hand": each card {@link #cards()} names that is in exile is put into its owner's hand
 * (CR 400.3). It follows a step that exiled the cards and, usually, a step that may have moved them on, read as
 * {@code Subjects.cardsThisWay(StepKind.EXILE, filter)}: "you may cast it without paying its mana cost … If you don't,
 * put that card into your hand" after {@link MayCastDiscardedCardWithoutPayingManaCostEffect} (Breaching Dragonstorm,
 * Bre of Clan Stoutarm, Solstice Revelations: a cast card has left exile, so it is not named, CR 400.7), or the
 * declined half of "put that card onto the battlefield or into your hand" (Ketria, Illuna, Apex of Wishes). A card the
 * subject finds in another zone stays there.
 *
 * @param cards the cards put into their owners' hands
 */
public record PutCardsIntoHandEffect(CardSubject cards) implements CardEffect {

    public PutCardsIntoHandEffect {
        Objects.requireNonNull(cards, "cards");
        if (cards.declaresTarget()) {
            throw new IllegalArgumentException("The step names the cards an earlier step moved: " + cards);
        }
    }
}
