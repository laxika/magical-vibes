package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.MadnessCast;

@CardRegistration(set = "SOI", collectorNumber = "142")
public class TwinsOfMaurerEstate extends Card {

    public TwinsOfMaurerEstate() {
        // Madness {2}{B}
        addCastingOption(new MadnessCast("{2}{B}"));
    }
}
