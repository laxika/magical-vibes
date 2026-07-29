package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.RegisterDelayedSelfReturnFromGraveyardEffect;

@CardRegistration(set = "MIR", collectorNumber = "318")
public class SandGolem extends Card {

    public SandGolem() {
        addEffect(EffectSlot.ON_SELF_DISCARDED_BY_OPPONENT,
                new RegisterDelayedSelfReturnFromGraveyardEffect(CounterType.PLUS_ONE_PLUS_ONE, 1));
    }
}
