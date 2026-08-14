package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.TriggeringPermanentConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledBySourceControllerPredicate;

@CardRegistration(set = "5DN", collectorNumber = "91")
public class RiteOfPassage extends Card {

    public RiteOfPassage() {
        // Whenever a creature you control is dealt damage, put a +1/+1 counter on it.
        addEffect(EffectSlot.ON_ANY_CREATURE_DEALT_DAMAGE, new TriggeringPermanentConditionalEffect(
                new PermanentControlledBySourceControllerPredicate(),
                new PutCounterOnTargetPermanentEffect(CounterType.PLUS_ONE_PLUS_ONE)));
    }
}
