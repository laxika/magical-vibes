package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.PutCountersEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "ORI", collectorNumber = "108")
@CardRegistration(set = "ANB", collectorNumber = "51")
public class MalakirCullblade extends Card {

    public MalakirCullblade() {
        // Whenever a creature an opponent controls dies, put a +1/+1 counter on this creature.
        addTrigger(Triggers.creatureDies(ObjectRelation.OPPONENT_CONTROLS), PutCountersEffect.onSelf(CounterType.PLUS_ONE_PLUS_ONE));
    }
}
