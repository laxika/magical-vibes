package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;

@CardRegistration(set = "GTC", collectorNumber = "197")
@CardRegistration(set = "GRN", collectorNumber = "198")
@CardRegistration(set = "RAV", collectorNumber = "232")
public class SkyknightLegionnaire extends Card {

    public SkyknightLegionnaire() {
        // Flying and haste are Scryfall-loaded keywords — no engine logic needed.
    }
}
