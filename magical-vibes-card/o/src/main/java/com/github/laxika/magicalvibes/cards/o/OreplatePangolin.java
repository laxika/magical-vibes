package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.model.effect.UnlessPaysEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.effect.PutCountersEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "EOE", collectorNumber = "150")
public class OreplatePangolin extends Card {

    public OreplatePangolin() {
        addTrigger(Triggers.enters(Triggers.ARTIFACT, ObjectRelation.ANOTHER_YOU_CONTROL),
                UnlessPaysEffect.youMayPay("{1}", PutCountersEffect.onSelf(CounterType.PLUS_ONE_PLUS_ONE))
                        .withPrompt("Pay {1} to put a +1/+1 counter on Oreplate Pangolin?"));
    }
}
