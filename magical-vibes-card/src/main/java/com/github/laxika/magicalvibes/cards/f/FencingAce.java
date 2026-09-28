package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;

@CardRegistration(set = "RTR", collectorNumber = "11")
@CardRegistration(set = "M20", collectorNumber = "16")
@CardRegistration(set = "DDL", collectorNumber = "5")
@CardRegistration(set = "A25", collectorNumber = "13")
@CardRegistration(set = "2XM", collectorNumber = "15")
@CardRegistration(set = "ANB", collectorNumber = "7")
@CardRegistration(set = "CMM", collectorNumber = "23")
public class FencingAce extends Card {

    public FencingAce() {
        // Double strike is loaded from Scryfall metadata; no engine logic needed.
    }
}
