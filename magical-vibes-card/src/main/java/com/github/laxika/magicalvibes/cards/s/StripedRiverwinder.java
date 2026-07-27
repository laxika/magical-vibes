package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;


@CardRegistration(set = "HOU", collectorNumber = "48")
public class StripedRiverwinder extends Card {

    public StripedRiverwinder() {
        // Hexproof is auto-loaded from Scryfall metadata.

        // Cycling {U} ({U}, Discard this card: Draw a card.) — discard cost is intrinsic.
        addCycling("{U}");
    }
}
