package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.condition.CastFromZone;
import com.github.laxika.magicalvibes.model.condition.NotCondition;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.EnterWithCountersEffect;
import com.github.laxika.magicalvibes.model.effect.ExileSourceCardWithSuspendCountersEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "FUT", collectorNumber = "162")
@CardRegistration(set = "MMA", collectorNumber = "205")
@CardRegistration(set = "C14", collectorNumber = "238")
@CardRegistration(set = "CM2", collectorNumber = "187")
public class Epochrasite extends Card {

    public Epochrasite() {
        addStaticEffect(new ConditionalEffect(new NotCondition(new CastFromZone(Zone.HAND)),
                new EnterWithCountersEffect(CounterType.PLUS_ONE_PLUS_ONE, new Fixed(3))));
        addTrigger(Triggers.dies(ObjectRelation.THIS), new ExileSourceCardWithSuspendCountersEffect(3));
    }
}
