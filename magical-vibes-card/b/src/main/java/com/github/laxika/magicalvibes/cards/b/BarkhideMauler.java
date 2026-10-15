package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;

@CardRegistration(set = "ONS", collectorNumber = "246")
public class BarkhideMauler extends Card {

    public BarkhideMauler() {
        addCycling("{2}");
    }
}
