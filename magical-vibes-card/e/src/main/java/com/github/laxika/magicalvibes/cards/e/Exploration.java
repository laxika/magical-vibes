package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.PlaysAdditionalLandEachTurnEffect;

@CardRegistration(set = "USG", collectorNumber = "250")
@CardRegistration(set = "2XM", collectorNumber = "167")
@CardRegistration(set = "DMR", collectorNumber = "159")
@CardRegistration(set = "AA3", collectorNumber = "17")
public class Exploration extends Card {

    public Exploration() {
        addStaticEffect(new PlaysAdditionalLandEachTurnEffect(1));
    }
}
