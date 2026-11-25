package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.effect.PutCountersEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "MSC", collectorNumber = "624")
public class ImperialCosmographer extends Card {

    public ImperialCosmographer() {
        addTrigger(Triggers.leavesBattlefieldWithoutDying(Triggers.CREATURE, ObjectRelation.ANOTHER_YOU_CONTROL),
                PutCountersEffect.onSelf(CounterType.PLUS_ONE_PLUS_ONE, 2));
    }
}
