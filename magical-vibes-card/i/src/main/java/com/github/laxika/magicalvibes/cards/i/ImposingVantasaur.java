package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;


@CardRegistration(set = "IKO", collectorNumber = "17")
public class ImposingVantasaur extends Card {

    public ImposingVantasaur() {
        // Vigilance is an intrinsic keyword (auto-loaded from Scryfall).
        // Cycling {1} ({1}, Discard this card: Draw a card.) — discard cost is intrinsic.
        addCycling("{1}");
    }
}
