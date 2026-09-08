package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ForetellCast;

@CardRegistration(set = "KHM", collectorNumber = "13")
public class GodsHallGuardian extends Card {

    public GodsHallGuardian() {
        addCastingOption(new ForetellCast("{3}{W}"));
    }
}
