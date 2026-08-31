package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;

@CardRegistration(set = "ONS", collectorNumber = "172")
public class SpinedBasher extends Card {

    public SpinedBasher() {
        addMorph("{2}{B}");
    }
}
