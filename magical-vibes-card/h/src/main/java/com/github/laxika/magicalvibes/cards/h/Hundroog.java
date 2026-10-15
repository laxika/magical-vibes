package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;


@CardRegistration(set = "LGN", collectorNumber = "129")
public class Hundroog extends Card {

    public Hundroog() {
        addCycling("{3}");
    }
}
