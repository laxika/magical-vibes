package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CountersOnSource;
import com.github.laxika.magicalvibes.model.condition.NoCreaturesAttackedThisTurn;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToEndStepPlayerEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;

@CardRegistration(set = "C16", collectorNumber = "16")
public class ChargingCinderhorn extends Card {

    public ChargingCinderhorn() {
        addEffect(EffectSlot.END_STEP_TRIGGERED, new ConditionalEffect(
                new NoCreaturesAttackedThisTurn(),
                SequenceEffect.of(
                        new PutCountersOnSelfEffect(CounterType.FURY),
                        new DealDamageToEndStepPlayerEffect(new CountersOnSource(CounterType.FURY)))));
    }
}
