package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;

@CardRegistration(set = "MH1", collectorNumber = "99")
public class NinjaOfTheNewMoon extends Card {

    public NinjaOfTheNewMoon() {
        addNinjutsu("{3}{B}");
    }
}
