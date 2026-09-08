package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ForetellCast;

@CardRegistration(set = "KHM", collectorNumber = "246")
public class ScornEffigy extends Card {

    public ScornEffigy() {
        addCastingOption(new ForetellCast("{0}"));
    }
}
