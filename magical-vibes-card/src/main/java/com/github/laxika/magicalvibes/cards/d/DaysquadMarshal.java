package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;

@CardRegistration(set = "IKO", collectorNumber = "8")
public class DaysquadMarshal extends Card {

    public DaysquadMarshal() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, CreateTokenEffect.whiteSoldier(1));
    }
}
