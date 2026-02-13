package com.github.laxika.magicalvibes.cards;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardRarity;

import java.util.function.Supplier;

public record CardPrinting(String setCode, String collectorNumber, Supplier<Card> factory, String flavorText,
                           String artist, CardRarity rarity) {

    public CardPrinting(String setCode, String collectorNumber, Supplier<Card> factory, String artist,
                        CardRarity rarity) {
        this(setCode, collectorNumber, factory, null, artist, rarity);
    }

    public Card createCard() {
        Card card = factory.get();
        card.setSetCode(setCode);
        card.setCollectorNumber(collectorNumber);
        card.setFlavorText(flavorText);
        card.setArtist(artist);
        card.setRarity(rarity);
        return card;
    }
}
