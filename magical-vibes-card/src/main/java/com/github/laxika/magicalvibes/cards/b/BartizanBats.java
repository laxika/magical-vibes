package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;

@CardRegistration(set = "M20", collectorNumber = "319")
@CardRegistration(set = "GRN", collectorNumber = "62")
public class BartizanBats extends Card {

    public BartizanBats() {
        // Flying is loaded from Scryfall; no extra engine logic is needed.
    }
}
