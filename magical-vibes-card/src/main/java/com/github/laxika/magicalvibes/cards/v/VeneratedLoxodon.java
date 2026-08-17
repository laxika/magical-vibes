package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnConvokeCreaturesEffect;

@CardRegistration(set = "GRN", collectorNumber = "30")
public class VeneratedLoxodon extends Card {

    public VeneratedLoxodon() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new PutCounterOnConvokeCreaturesEffect(CounterType.PLUS_ONE_PLUS_ONE));
    }
}
