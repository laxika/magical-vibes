package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ControllerDamageMultiplierEffect;

@CardRegistration(set = "MOM", collectorNumber = "135")
public class CityOnFire extends Card {

    public CityOnFire() {
        addEffect(EffectSlot.STATIC, new ControllerDamageMultiplierEffect(3, null, true));
    }
}
