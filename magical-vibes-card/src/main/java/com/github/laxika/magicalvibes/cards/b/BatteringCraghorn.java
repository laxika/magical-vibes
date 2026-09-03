package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;

@CardRegistration(set = "ONS", collectorNumber = "188")
public class BatteringCraghorn extends Card {

    public BatteringCraghorn() {
        addMorph("{1}{R}{R}");
    }
}
