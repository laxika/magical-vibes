package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;

@CardRegistration(set = "FUT", collectorNumber = "145")
@CardRegistration(set = "MMA", collectorNumber = "148")
@CardRegistration(set = "TSR", collectorNumber = "211")
public class Imperiosaur extends Card {

    public Imperiosaur() {
        setRequiresBasicLandMana(true);
    }
}
