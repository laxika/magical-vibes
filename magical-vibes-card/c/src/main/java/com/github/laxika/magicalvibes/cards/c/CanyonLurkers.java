package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;

@CardRegistration(set = "KTK", collectorNumber = "105")
public class CanyonLurkers extends Card {

    public CanyonLurkers() {
        addMorph("{3}{R}");
    }
}
