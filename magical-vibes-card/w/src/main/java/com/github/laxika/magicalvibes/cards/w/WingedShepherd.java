package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;


@CardRegistration(set = "AKH", collectorNumber = "39")
public class WingedShepherd extends Card {

    public WingedShepherd() {
        // Flying and vigilance are intrinsic keywords (auto-loaded from Scryfall).
        // Cycling {W} ({W}, Discard this card: Draw a card.) — discard cost is intrinsic.
        addCycling("{W}");
    }
}
