package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DoubleControllerDamageEffect;

@CardRegistration(set = "MOM", collectorNumber = "135")
@CardRegistration(set = "MOM", collectorNumber = "363")
@CardRegistration(set = "SLD", collectorNumber = "1803")
public class CityOnFire extends Card {

    public CityOnFire() {
        addStaticEffect(new DoubleControllerDamageEffect(3, null, true));
    }
}
