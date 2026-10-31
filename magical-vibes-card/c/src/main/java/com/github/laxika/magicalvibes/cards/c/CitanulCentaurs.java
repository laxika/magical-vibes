package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.RegisterEchoAtNextUpkeepEffect;

@CardRegistration(set = "USG", collectorNumber = "243")
public class CitanulCentaurs extends Card {

    public CitanulCentaurs() {
        addStaticEffect(new RegisterEchoAtNextUpkeepEffect("{3}{G}"));
    }
}
