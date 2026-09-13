package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;

@CardRegistration(set = "ONS", collectorNumber = "270")
@CardRegistration(set = "A25", collectorNumber = "177")
public class KrosanColossus extends Card {

    public KrosanColossus() {
        addMorph("{6}{G}{G}");
    }
}
