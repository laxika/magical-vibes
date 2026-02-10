package com.github.laxika.magicalvibes.cards;

import com.github.laxika.magicalvibes.model.Card;

import java.util.function.Supplier;

public record CardPrinting(String setCode, String collectorNumber, Supplier<Card> factory, String flavorText) {

    public CardPrinting(String setCode, String collectorNumber, Supplier<Card> factory) {
        this(setCode, collectorNumber, factory, null);
    }

    public Card createCard() {
        Card card = factory.get();
        card.setSetCode(setCode);
        card.setCollectorNumber(collectorNumber);
        card.setFlavorText(flavorText);
        return card;
    }
}
