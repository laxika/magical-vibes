package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.EventValue;
import com.github.laxika.magicalvibes.model.condition.Kicked;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenCopyOfSourceEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.KickerEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "BLC", collectorNumber = "25")
@CardRegistration(set = "BLC", collectorNumber = "59")
public class ProsperousBandit extends Card {

    public ProsperousBandit() {
        addStaticEffect(new KickerEffect("{1}"));
        addTrigger(Triggers.enters(ObjectRelation.THIS), new ConditionalEffect(new Kicked(),
                new CreateTokenCopyOfSourceEffect(false, 1, null, null, false, 1, 1)));
        addTrigger(Triggers.dealsCombatDamageToPlayer(),
                CreateTokenEffect.ofTreasureToken(new EventValue(), true));
    }
}
