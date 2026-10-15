package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.MadnessCast;

@CardRegistration(set = "SOI", collectorNumber = "166")
public class IncorrigibleYouths extends Card {

    public IncorrigibleYouths() {
        addCastingOption(new MadnessCast("{2}{R}"));
    }
}
