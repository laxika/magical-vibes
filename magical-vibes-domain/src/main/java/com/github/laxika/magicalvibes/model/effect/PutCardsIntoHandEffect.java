package com.github.laxika.magicalvibes.model.effect;

import java.util.Objects;

/**
 * "Put that card into your hand", "return it to its owner's hand": each card {@link #cards()} names that is in a
 * graveyard ("a permanent card from among the cards milled this way", "that card" of a dies trigger), in exile, in the
 * command zone ("put your commander into your hand from the command zone"), or revealed in a library
 * ("then puts it into their hand": {@code Subjects.cardsThisWay(StepKind.REVEAL)}, Duskmantle Seer), is put into its
 * owner's hand (CR 400.3). It follows a step that exiled the cards and, usually, a step that may have moved them on, read as
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
        if (cards.inHiddenZone()) {
            throw new IllegalArgumentException("The step names cards in a public zone or revealed ones: " + cards);
        }
    }

    @Override
    public TargetSpec targetSpec() {
        return cards.targetSpec();
    }

    @Override
    public boolean targetsAllExiledCardsInAbility() {
        return cards instanceof CardSubject.TargetExiledCard target && !target.exiledWithSource();
    }
}
