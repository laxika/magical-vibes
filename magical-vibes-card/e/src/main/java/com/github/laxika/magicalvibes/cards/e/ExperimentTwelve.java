package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.amount.SourcePower;
import com.github.laxika.magicalvibes.model.effect.PermanentReference;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnReferencedPermanentEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "MKC", collectorNumber = "37")
@CardRegistration(set = "MKC", collectorNumber = "347")
public class ExperimentTwelve extends Card {

    public ExperimentTwelve() {
        addMorph("{4}{G}");
        addTrigger(Triggers.turnedFaceUp(Triggers.CREATURE, ObjectRelation.YOU_CONTROL),
                new PutCounterOnReferencedPermanentEffect(
                        PermanentReference.TRIGGERING,
                        CounterType.PLUS_ONE_PLUS_ONE,
                        new SourcePower()));
    }
}
