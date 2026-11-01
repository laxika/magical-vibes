package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.effect.DrawCardIfDyingSourceHadCounterEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "STX", collectorNumber = "85")
public class PromisingDuskmage extends Card {

    public PromisingDuskmage() {
        addTrigger(Triggers.dies(ObjectRelation.THIS),
                new DrawCardIfDyingSourceHadCounterEffect(CounterType.PLUS_ONE_PLUS_ONE));
    }
}
