package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.Card;

import java.util.List;
import java.util.UUID;

/** Continuation that puts cards not put onto the battlefield on the bottom of a library. */
public record MorphicTideBottomCardsEffect(UUID playerId, List<Card> cards, boolean randomOrder)
        implements CardEffect {

    public MorphicTideBottomCardsEffect(UUID playerId, List<Card> cards) {
        this(playerId, cards, false);
    }

    public MorphicTideBottomCardsEffect {
        cards = List.copyOf(cards);
    }
}
