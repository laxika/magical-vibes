package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;

@CardRegistration(set = "ONS", collectorNumber = "20")
public class DaruLancer extends Card {

    public DaruLancer() {
        addMorph("{2}{W}{W}");
    }
}
