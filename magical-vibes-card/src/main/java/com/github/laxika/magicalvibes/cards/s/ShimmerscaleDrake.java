package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;


@CardRegistration(set = "AKH", collectorNumber = "70")
@CardRegistration(set = "AKR", collectorNumber = "78")
public class ShimmerscaleDrake extends Card {

    public ShimmerscaleDrake() {
        // Flying is an intrinsic keyword (auto-loaded from Scryfall).
        // Cycling {2} ({2}, Discard this card: Draw a card.) — discard cost is intrinsic.
        addCycling("{2}");
    }
}
