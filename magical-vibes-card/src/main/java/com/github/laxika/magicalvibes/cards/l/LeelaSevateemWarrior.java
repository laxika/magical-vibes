package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ExceptFirstDrawStepTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;

@CardRegistration(set = "WHO", collectorNumber = "107")
public class LeelaSevateemWarrior extends Card {

    public LeelaSevateemWarrior() {
        addEffect(EffectSlot.ON_OPPONENT_DRAWS,
                new ExceptFirstDrawStepTriggerEffect(
                        new PutCountersOnSelfEffect(CounterType.PLUS_ONE_PLUS_ONE)));
    }
}
