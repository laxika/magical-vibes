package com.github.laxika.magicalvibes.model;

import java.util.List;

/** An immutable, resolved deck. Card instances are owned and frozen when the game starts. */
public record DeckDefinition(List<Card> mainDeck, List<Card> sideboard, Card commander) {
    public DeckDefinition {
        mainDeck = List.copyOf(mainDeck);
        sideboard = List.copyOf(sideboard);
    }
}
