package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.RegisterEchoAtNextUpkeepEffect;

@CardRegistration(set = "USG", collectorNumber = "193")
@CardRegistration(set = "VMA", collectorNumber = "169")
public class GoblinPatrol extends Card {

    public GoblinPatrol() {
        addStaticEffect(new RegisterEchoAtNextUpkeepEffect("{R}"));
    }
}
