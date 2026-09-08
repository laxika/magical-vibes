package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;

@CardRegistration(set = "BRO", collectorNumber = "204")
public class RustGoliath extends Card {

    public RustGoliath() {
        addPrototype("{3}{G}{G}", CardColor.GREEN, 3, 5);
    }
}
