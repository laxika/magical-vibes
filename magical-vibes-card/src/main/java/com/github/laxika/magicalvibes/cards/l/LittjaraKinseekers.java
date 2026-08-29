package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.ControlsCreaturesSharingCreatureType;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;
import com.github.laxika.magicalvibes.model.effect.ScryEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;

@CardRegistration(set = "KHM", collectorNumber = "66")
public class LittjaraKinseekers extends Card {

    public LittjaraKinseekers() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new ConditionalEffect(new ControlsCreaturesSharingCreatureType(3),
                        SequenceEffect.of(
                                new PutCountersOnSelfEffect(CounterType.PLUS_ONE_PLUS_ONE),
                                new ScryEffect(1))));
    }
}
