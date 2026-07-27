package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;


@CardRegistration(set = "HOU", collectorNumber = "4")
public class AngelOfTheGodPharaoh extends Card {

    public AngelOfTheGodPharaoh() {
        // Flying is an intrinsic keyword (auto-loaded from Scryfall).
        // Cycling {2} ({2}, Discard this card: Draw a card.) — discard cost is intrinsic.
        addCycling("{2}");
    }
}
