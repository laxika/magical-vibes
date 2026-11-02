package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.effect.DrawCardForEachDyingSourceCounterEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSourceEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "GN3", collectorNumber = "3")
public class VogarNecropolisTyrant extends Card {

    public VogarNecropolisTyrant() {
        // Whenever another creature dies during your turn, put a +1/+1 counter on Vogar.
        addTrigger(Triggers.creatureDies(ObjectRelation.ANOTHER).onlyDuringYourTurn(),
                new PutCountersOnSourceEffect(1, 1, 1));

        // When Vogar dies, draw a card for each +1/+1 counter on it.
        addTrigger(Triggers.dies(ObjectRelation.THIS),
                new DrawCardForEachDyingSourceCounterEffect(CounterType.PLUS_ONE_PLUS_ONE));
    }
}
