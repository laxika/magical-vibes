package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.condition.GiftPromised;
import com.github.laxika.magicalvibes.model.condition.NotCondition;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.ControlEnchantedCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.EachOtherPlayerDrawsCardEffect;
import com.github.laxika.magicalvibes.model.effect.GiftEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.effect.TapPermanentsEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "BLB", collectorNumber = "53")
public class Kitnap extends Card {

    public Kitnap() {
        addStaticEffect(new GiftEffect());
        addTrigger(Triggers.enters(ObjectRelation.THIS),
                TriggerOptions.DEFAULT.withInterveningIf(new GiftPromised()),
                new EachOtherPlayerDrawsCardEffect(1));
        target(TargetFilters.creature())
                .addStaticEffect(new ControlEnchantedCreatureEffect())
                .addTrigger(Triggers.enters(ObjectRelation.THIS),
                        new TapPermanentsEffect(Subjects.target()),
                        new ConditionalEffect(new NotCondition(new GiftPromised()),
                                new PutCountersEffect(CounterType.STUN, 3)));
    }
}
