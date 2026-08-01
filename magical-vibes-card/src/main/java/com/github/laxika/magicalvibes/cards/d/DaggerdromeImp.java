package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;

@CardRegistration(set = "RTR", collectorNumber = "60")
public class DaggerdromeImp extends Card {

    public DaggerdromeImp() {
        // Flying and lifelink are keywords loaded from Scryfall; no extra engine logic needed.
    }
}
