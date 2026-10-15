package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;

@CardRegistration(set = "IKO", collectorNumber = "124")
public class LavaSerpent extends Card {

    public LavaSerpent() {
        // Haste is an intrinsic keyword loaded from Scryfall.
        addCycling("{2}");
    }
}
