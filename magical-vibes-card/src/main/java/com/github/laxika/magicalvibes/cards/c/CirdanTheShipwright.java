package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CirdanTheShipwrightEffect;

@CardRegistration(set = "LTC", collectorNumber = "50")
@CardRegistration(set = "LTC", collectorNumber = "133")
public class CirdanTheShipwright extends Card {

    public CirdanTheShipwright() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new CirdanTheShipwrightEffect());
        addEffect(EffectSlot.ON_ATTACK, new CirdanTheShipwrightEffect());
    }
}
