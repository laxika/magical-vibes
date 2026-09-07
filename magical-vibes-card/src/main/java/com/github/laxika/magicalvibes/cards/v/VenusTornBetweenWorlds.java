package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.EventValue;
import com.github.laxika.magicalvibes.model.effect.AllyCombatDamageTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.MayPayManaEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasCountersPredicate;

@CardRegistration(set = "TMT", collectorNumber = "136")
public class VenusTornBetweenWorlds extends Card {

    public VenusTornBetweenWorlds() {
        addEffect(EffectSlot.ON_DEALT_DAMAGE,
                new PutCountersOnSelfEffect(CounterType.PLUS_ONE_PLUS_ONE, new EventValue()));
        addEffect(EffectSlot.ON_ALLY_CREATURE_COMBAT_DAMAGE_TO_PLAYER,
                new AllyCombatDamageTriggerEffect(
                        new PermanentHasCountersPredicate(CounterType.ANY),
                        new MayPayManaEffect("{U}", new DrawCardEffect(1),
                                "Pay {U} to draw a card?")));
    }
}
