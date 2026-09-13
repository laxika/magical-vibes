package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;

@CardRegistration(set = "PTK", collectorNumber = "19")
@CardRegistration(set = "ME3", collectorNumber = "23")
public class ShuCavalry extends Card {

    public ShuCavalry() {
        // Vanilla 2/2 with horsemanship — the keyword is auto-loaded from Scryfall
        // and enforced by the engine's blocking rules.
    }
}
