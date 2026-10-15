package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.ReduceDamageToControlledCreaturesEffect;

@CardRegistration(set = "PLS", collectorNumber = "9")
public class LashknifeBarrier extends Card {

    public LashknifeBarrier() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new DrawCardEffect(1));
        addEffect(EffectSlot.STATIC, new ReduceDamageToControlledCreaturesEffect(1));
    }
}
