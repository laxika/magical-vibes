package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;

@CardRegistration(set = "NEO", collectorNumber = "112")
public class MukotaiAmbusher extends Card {

    public MukotaiAmbusher() {
        addNinjutsu("{1}{B}");
    }
}
