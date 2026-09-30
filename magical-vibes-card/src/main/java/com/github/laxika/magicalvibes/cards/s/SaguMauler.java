package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;

@CardRegistration(set = "KTK", collectorNumber = "196")
@CardRegistration(set = "C19", collectorNumber = "200")
public class SaguMauler extends Card {

    public SaguMauler() {
        addMorph("{3}{G}{U}");
    }
}
