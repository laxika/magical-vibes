package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;

@CardRegistration(set = "KTK", collectorNumber = "201")
public class SnowhornRider extends Card {

    public SnowhornRider() {
        addMorph("{2}{G}{U}{R}");
    }
}
