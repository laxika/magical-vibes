package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "CSP", collectorNumber = "129")
public class GarzaZolPlagueQueen extends Card {

    public GarzaZolPlagueQueen() {
        addTrigger(Triggers.creatureDies(ObjectRelation.DAMAGED_BY_THIS), PutCountersEffect.onSelf(CounterType.PLUS_ONE_PLUS_ONE));
        addTrigger(Triggers.dealsCombatDamageToPlayer(),
                new MayEffect(new DrawCardEffect(1), "Draw a card?"));
    }
}
