package com.github.laxika.magicalvibes.model.effect;

import java.util.Objects;

/**
 * "Choose an instant or sorcery card exiled this way", "a creature card from among them", "one of them": the
 * controller chooses one of the cards {@code cards} names as the effect resolves (CR 608.2d). The card doesn't move.
 * The step resolves itself again with the answer and records it as {@code StepKind.CHOOSE}, so what is done with "it"
 * is a later step over {@code Subjects.cardsThisWay(StepKind.CHOOSE)}; with no card named it records nothing.
 * "You may ... one of them" is this inside a {@code MayEffect}, with what is done with the card.
 *
 * @param cards       among which cards
 * @param label       what the prompt calls one ("an instant or sorcery card exiled this way")
 * @param alwaysAsked whether the choice is announced when only one card qualifies, as a card that says "choose" has
 *                    it; otherwise a lone card is taken unasked ("put a creature card from among them onto the
 *                    battlefield")
 */
public record ChooseCardEffect(CardSubject cards, String label, boolean alwaysAsked) implements CardEffect {

    public ChooseCardEffect {
        Objects.requireNonNull(cards, "cards");
        Objects.requireNonNull(label, "label");
        if (cards.declaresTarget()) {
            throw new IllegalArgumentException("A resolution-time choice targets nothing: " + cards);
        }
    }

    /** "Choose [label]": announced also when only one card qualifies. */
    public static ChooseCardEffect announced(CardSubject cards, String label) {
        return new ChooseCardEffect(cards, label, true);
    }

    /** "[Label] from among them", "one of them": a lone card is the one. */
    public static ChooseCardEffect oneOf(CardSubject cards, String label) {
        return new ChooseCardEffect(cards, label, false);
    }
}
