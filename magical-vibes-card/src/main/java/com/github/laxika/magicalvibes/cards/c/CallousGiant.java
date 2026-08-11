package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.PreventDamageUpToAmountToSelfEffect;

@CardRegistration(set = "INV", collectorNumber = "139")
public class CallousGiant extends Card {

    public CallousGiant() {
        addEffect(EffectSlot.STATIC, new PreventDamageUpToAmountToSelfEffect(3));
    }
}
