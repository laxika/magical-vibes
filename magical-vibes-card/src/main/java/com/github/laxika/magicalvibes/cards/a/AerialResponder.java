package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;

@CardRegistration(set = "KLD", collectorNumber = "2")
@CardRegistration(set = "GNT", collectorNumber = "6")
@CardRegistration(set = "KLR", collectorNumber = "1")
public class AerialResponder extends Card {

    public AerialResponder() {
        // Flying, vigilance, and lifelink are loaded from Scryfall.
    }
}
