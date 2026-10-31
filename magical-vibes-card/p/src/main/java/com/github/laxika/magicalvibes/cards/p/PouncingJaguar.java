package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.RegisterEchoAtNextUpkeepEffect;

@CardRegistration(set = "USG", collectorNumber = "269")
public class PouncingJaguar extends Card {

    public PouncingJaguar() {
        addStaticEffect(new RegisterEchoAtNextUpkeepEffect("{G}"));
    }
}
