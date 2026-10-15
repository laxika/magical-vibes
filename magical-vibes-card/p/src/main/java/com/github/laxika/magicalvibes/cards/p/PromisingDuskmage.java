package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DrawCardIfDyingSourceHadCounterEffect;

@CardRegistration(set = "STX", collectorNumber = "85")
public class PromisingDuskmage extends Card {

    public PromisingDuskmage() {
        addEffect(EffectSlot.ON_DEATH,
                new DrawCardIfDyingSourceHadCounterEffect(CounterType.PLUS_ONE_PLUS_ONE));
    }
}
