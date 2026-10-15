package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;

@CardRegistration(set = "BRO", collectorNumber = "242")
public class ReconstructedThopter extends Card {

    public ReconstructedThopter() {
        addUnearth("{2}");
    }
}
