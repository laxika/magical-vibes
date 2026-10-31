package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.RegisterEchoAtNextUpkeepEffect;

@CardRegistration(set = "USG", collectorNumber = "245")
public class CradleGuard extends Card {

    public CradleGuard() {
        addStaticEffect(new RegisterEchoAtNextUpkeepEffect("{1}{G}{G}"));
    }
}
