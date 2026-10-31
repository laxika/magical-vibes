package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.RegisterEchoAtNextUpkeepEffect;

@CardRegistration(set = "USG", collectorNumber = "215")
public class ShivanRaptor extends Card {

    public ShivanRaptor() {
        addStaticEffect(new RegisterEchoAtNextUpkeepEffect("{2}{R}"));
    }
}
