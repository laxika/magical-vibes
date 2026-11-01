package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.effect.PermanentReference;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnReferencedPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSourceEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "MKM", collectorNumber = "233")
public class SumalaSentry extends Card {

    public SumalaSentry() {
        addTrigger(Triggers.turnedFaceUp(null, ObjectRelation.YOU_CONTROL),
                SequenceEffect.of(
                        new PutCounterOnReferencedPermanentEffect(
                                PermanentReference.TRIGGERING, CounterType.PLUS_ONE_PLUS_ONE),
                        new PutCountersOnSourceEffect(1, 1, 1)));
    }
}
