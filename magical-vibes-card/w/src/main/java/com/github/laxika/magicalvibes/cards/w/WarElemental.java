package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.amount.EventValue;
import com.github.laxika.magicalvibes.model.condition.NotCondition;
import com.github.laxika.magicalvibes.model.condition.OpponentDealtDamageThisTurn;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "MRD", collectorNumber = "112")
public class WarElemental extends Card {

    public WarElemental() {
        addTrigger(Triggers.enters(ObjectRelation.THIS), new ConditionalEffect(
                new NotCondition(new OpponentDealtDamageThisTurn(1)), new SacrificeSelfEffect()));
        addTrigger(Triggers.opponentDealtDamage(),
                PutCountersEffect.onSelf(CounterType.PLUS_ONE_PLUS_ONE, new EventValue()));
    }
}
