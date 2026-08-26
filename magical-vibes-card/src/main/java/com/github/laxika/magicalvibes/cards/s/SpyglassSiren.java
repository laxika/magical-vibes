package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;

@CardRegistration(set = "LCI", collectorNumber = "78")
public class SpyglassSiren extends Card {

    public SpyglassSiren() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, CreateTokenEffect.ofMapToken(1));
    }
}
