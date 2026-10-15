package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;

@CardRegistration(set = "KTK", collectorNumber = "20")
public class SageEyeHarrier extends Card {

    public SageEyeHarrier() {
        addMorph("{3}{W}");
    }
}
