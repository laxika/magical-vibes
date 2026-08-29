package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.OncePerTurnPerCreatureTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.PermanentReference;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnReferencedPermanentEffect;

@CardRegistration(set = "TDM", collectorNumber = "227")
public class StalwartSuccessor extends Card {

    public StalwartSuccessor() {
        addEffect(EffectSlot.ON_ALLY_COUNTER_PUT_ON_CREATURE,
                new OncePerTurnPerCreatureTriggerEffect(new PutCounterOnReferencedPermanentEffect(
                        PermanentReference.TRIGGERING, CounterType.PLUS_ONE_PLUS_ONE)));
    }
}
