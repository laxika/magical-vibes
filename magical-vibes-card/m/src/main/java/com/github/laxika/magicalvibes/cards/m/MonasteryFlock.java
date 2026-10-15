package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;

@CardRegistration(set = "KTK", collectorNumber = "47")
public class MonasteryFlock extends Card {

    public MonasteryFlock() {
        addMorph("{U}");
    }
}
