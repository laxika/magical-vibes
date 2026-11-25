package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.ConjureCardToHandEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;
import com.github.laxika.magicalvibes.model.effect.Subjects;

@CardRegistration(set = "HBG", collectorNumber = "39")
public class ThayanEvokers extends Card {

    public ThayanEvokers() {
        addTrigger(Triggers.enters(ObjectRelation.THIS),
                new ConjureCardToHandEffect("M10", "146"),
                new DiscardEffect(1, Subjects.you()));
        addTrigger(Triggers.youConjure(),
                PutCountersEffect.onSelf(CounterType.PLUS_ONE_PLUS_ONE));
    }
}
