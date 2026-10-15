package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;


@CardRegistration(set = "AKH", collectorNumber = "116")
@CardRegistration(set = "AKR", collectorNumber = "135")
public class WastelandScorpion extends Card {

    public WastelandScorpion() {
        // Deathtouch is a Scryfall-loaded keyword; no engine logic needed.

        // Cycling {2} ({2}, Discard this card: Draw a card.) — discard cost is intrinsic.
        addCycling("{2}");
    }
}
