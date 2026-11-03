package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;
import com.github.laxika.magicalvibes.model.effect.ScryEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "TDM", collectorNumber = "18")
public class PoisedPractitioner extends Card {

    public PoisedPractitioner() {
        addTrigger(Triggers.youCastSpell().nthThisTurn(2),
                SequenceEffect.of(new PutCountersOnSelfEffect(CounterType.PLUS_ONE_PLUS_ONE), new ScryEffect(1)));
    }
}
