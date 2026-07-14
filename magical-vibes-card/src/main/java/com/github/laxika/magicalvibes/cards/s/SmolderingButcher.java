package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;

@CardRegistration(set = "EVE", collectorNumber = "42")
public class SmolderingButcher extends Card {

    public SmolderingButcher() {
        // Vanilla 4/2 with wither — the keyword is auto-loaded from Scryfall and drives the
        // -1/-1 counter combat damage in the engine, so no constructor logic is required.
    }
}
