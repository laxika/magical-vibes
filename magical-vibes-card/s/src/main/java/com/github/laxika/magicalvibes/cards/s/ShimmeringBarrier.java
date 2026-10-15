package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;


@CardRegistration(set = "USG", collectorNumber = "50")
public class ShimmeringBarrier extends Card {

    public ShimmeringBarrier() {
        // First strike and defender are intrinsic keywords (auto-loaded from Scryfall).
        addCycling("{2}");
    }
}
