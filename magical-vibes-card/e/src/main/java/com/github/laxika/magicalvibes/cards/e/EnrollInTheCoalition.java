package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.GrantControllerFlagbearerEffect;

@CardRegistration(set = "MB1", collectorNumber = "4")
public class EnrollInTheCoalition extends Card {

    public EnrollInTheCoalition() {
        addStaticEffect(new GrantControllerFlagbearerEffect());
    }
}
