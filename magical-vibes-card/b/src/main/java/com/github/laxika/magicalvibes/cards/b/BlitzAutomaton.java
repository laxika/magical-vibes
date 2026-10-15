package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;

@CardRegistration(set = "BRO", collectorNumber = "158")
public class BlitzAutomaton extends Card {

    public BlitzAutomaton() {
        addPrototype("{2}{R}", CardColor.RED, 3, 2);
    }
}
