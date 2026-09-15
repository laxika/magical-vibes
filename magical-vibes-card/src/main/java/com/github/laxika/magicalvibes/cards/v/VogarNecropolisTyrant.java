package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.ControllerTurn;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardForEachDyingSourceCounterEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSourceEffect;

@CardRegistration(set = "GN3", collectorNumber = "3")
public class VogarNecropolisTyrant extends Card {

    public VogarNecropolisTyrant() {
        // Whenever another creature dies during your turn, put a +1/+1 counter on Vogar.
        addEffect(EffectSlot.ON_ANY_CREATURE_DIES,
                new ConditionalEffect(new ControllerTurn(), new PutCountersOnSourceEffect(1, 1, 1)));

        // When Vogar dies, draw a card for each +1/+1 counter on it.
        addEffect(EffectSlot.ON_DEATH,
                new DrawCardForEachDyingSourceCounterEffect(CounterType.PLUS_ONE_PLUS_ONE));
    }
}
