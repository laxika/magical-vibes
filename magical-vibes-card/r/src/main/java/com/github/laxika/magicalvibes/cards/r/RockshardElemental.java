package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;

@CardRegistration(set = "LGN", collectorNumber = "109")
public class RockshardElemental extends Card {

    public RockshardElemental() {
        addMorph("{4}{R}{R}");
    }
}
