package com.github.laxika.magicalvibes.model.effect;

/** Each opponent with more than the threshold number of cards discards down to that number. */
public record EachOpponentDiscardsDownToHandSizeEffect(int handSize, CardEffect thenEffect)
        implements CardEffect {

    public EachOpponentDiscardsDownToHandSizeEffect(int handSize) {
        this(handSize, null);
    }
}
