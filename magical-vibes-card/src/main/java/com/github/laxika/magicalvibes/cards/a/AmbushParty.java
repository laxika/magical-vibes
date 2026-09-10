package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.cards.CardRegistration;

@CardRegistration(set = "5ED", collectorNumber = "208")
@CardRegistration(set = "HML", collectorNumber = "63a")
@CardRegistration(set = "HML", collectorNumber = "63b")
@CardRegistration(set = "ME2", collectorNumber = "115")
public class AmbushParty extends Card {

    public AmbushParty() {
        // First strike and Haste are auto-loaded from Scryfall. No other engine logic.
    }
}
