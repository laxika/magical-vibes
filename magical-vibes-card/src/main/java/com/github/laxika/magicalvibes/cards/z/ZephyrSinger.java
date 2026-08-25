package com.github.laxika.magicalvibes.cards.z;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnConvokeCreaturesEffect;

@CardRegistration(set = "MOM", collectorNumber = "86")
public class ZephyrSinger extends Card {

    public ZephyrSinger() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new PutCounterOnConvokeCreaturesEffect(CounterType.FLYING));
    }
}
