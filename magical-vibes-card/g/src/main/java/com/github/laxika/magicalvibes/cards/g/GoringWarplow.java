package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;

@CardRegistration(set = "BRO", collectorNumber = "120")
public class GoringWarplow extends Card {

    public GoringWarplow() {
        addPrototype("{1}{B}", CardColor.BLACK, 1, 1);
    }
}
