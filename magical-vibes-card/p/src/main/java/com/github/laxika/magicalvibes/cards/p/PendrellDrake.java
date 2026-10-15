package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;

@CardRegistration(set = "USG", collectorNumber = "86")
public class PendrellDrake extends Card {

    public PendrellDrake() {
        addCycling("{2}");
    }
}
