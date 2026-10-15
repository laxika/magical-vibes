package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.MayPayManaEffect;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnEachMatchingPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.EachPermanentScope;
import com.github.laxika.magicalvibes.model.effect.QueueReflexiveAbilityEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsTriggeringPermanentPredicate;

@CardRegistration(set = "2X2", collectorNumber = "6")
public class AnointerOfValor extends Card {

    public AnointerOfValor() {
        addEffect(EffectSlot.ON_ANY_CREATURE_ATTACKS, new MayPayManaEffect(
                "{3}",
                new QueueReflexiveAbilityEffect(new PutCounterOnEachMatchingPermanentEffect(
                        CounterType.PLUS_ONE_PLUS_ONE, 1, new PermanentIsTriggeringPermanentPredicate(),
                        EachPermanentScope.ALL_PLAYERS), false, false, true),
                "Pay {3} to put a +1/+1 counter on the attacking creature?"));
    }
}
