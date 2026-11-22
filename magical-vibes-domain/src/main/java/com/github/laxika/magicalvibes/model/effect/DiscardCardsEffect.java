package com.github.laxika.magicalvibes.model.effect;

import java.util.Objects;

/**
 * "Discards all nonland cards", "discards each nonland card revealed this way", "you choose a card ... that player
 * discards it": the cards {@link #cards()} names that are in a hand are discarded by their owners, each player's as
 * one event; nobody chooses. {@code DiscardEffect} is the discard whose cards are chosen as it is made. Records
 * {@code StepKind.DISCARD}: "if you do", "for each card discarded this way".
 *
 * @param cards which cards are discarded
 */
public record DiscardCardsEffect(CardSubject cards) implements CardEffect {

    public DiscardCardsEffect {
        Objects.requireNonNull(cards, "cards");
    }

    @Override
    public TargetSpec targetSpec() {
        return cards.targetSpec();
    }
}
