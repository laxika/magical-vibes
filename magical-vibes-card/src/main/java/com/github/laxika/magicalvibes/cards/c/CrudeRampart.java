package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;

@CardRegistration(set = "ONS", collectorNumber = "17")
public class CrudeRampart extends Card {

    public CrudeRampart() {
        addMorph("{4}{W}");
    }
}
