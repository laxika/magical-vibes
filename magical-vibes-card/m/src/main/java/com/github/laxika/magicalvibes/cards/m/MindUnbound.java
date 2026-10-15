package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnSelfThenDrawPerCounterEffect;

@CardRegistration(set = "M12", collectorNumber = "68")
public class MindUnbound extends Card {

    public MindUnbound() {
        addEffect(EffectSlot.UPKEEP_TRIGGERED, new PutCounterOnSelfThenDrawPerCounterEffect(CounterType.LORE));
    }
}
