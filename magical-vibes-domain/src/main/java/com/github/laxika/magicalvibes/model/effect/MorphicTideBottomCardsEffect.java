package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.Card;

import java.util.List;
import java.util.UUID;

/** Continuation that orders Morphic Tide's cards not put onto the battlefield. */
public record MorphicTideBottomCardsEffect(UUID playerId, List<Card> cards) implements CardEffect {

    public MorphicTideBottomCardsEffect {
        cards = List.copyOf(cards);
    }
}
