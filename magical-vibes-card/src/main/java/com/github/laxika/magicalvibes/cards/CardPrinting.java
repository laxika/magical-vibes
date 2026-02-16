package com.github.laxika.magicalvibes.cards;

import com.github.laxika.magicalvibes.model.Card;

import java.util.function.Supplier;

public record CardPrinting(String setCode, String collectorNumber, Supplier<Card> factory) {

    public Card createCard() {
        Card card = factory.get();
        card.setSetCode(setCode);
        card.setCollectorNumber(collectorNumber);
        return card;
    }
}
