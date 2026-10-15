package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ForetellCast;

@CardRegistration(set = "KHM", collectorNumber = "116")
public class VengefulReaper extends Card {

    public VengefulReaper() {
        addCastingOption(new ForetellCast("{1}{B}"));
    }
}
