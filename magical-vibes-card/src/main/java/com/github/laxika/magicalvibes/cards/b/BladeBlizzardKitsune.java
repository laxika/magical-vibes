package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;

@CardRegistration(set = "NEO", collectorNumber = "5")
public class BladeBlizzardKitsune extends Card {

    public BladeBlizzardKitsune() {
        addNinjutsu("{3}{W}");
    }
}
