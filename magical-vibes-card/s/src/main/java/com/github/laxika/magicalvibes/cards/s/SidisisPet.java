package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;

@CardRegistration(set = "KTK", collectorNumber = "90")
public class SidisisPet extends Card {

    public SidisisPet() {
        addMorph("{1}{B}");
    }
}
