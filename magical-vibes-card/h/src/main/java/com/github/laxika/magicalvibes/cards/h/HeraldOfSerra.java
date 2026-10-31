package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.RegisterEchoAtNextUpkeepEffect;

@CardRegistration(set = "USG", collectorNumber = "17")
public class HeraldOfSerra extends Card {

    public HeraldOfSerra() {
        addStaticEffect(new RegisterEchoAtNextUpkeepEffect("{2}{W}{W}"));
    }
}
